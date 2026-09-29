package com.fx.migration

import com.fx.migration.legacy.LegacyCollections
import com.fx.migration.legacy.LegacyDocument
import com.fx.migration.legacy.LegacyMongoReader
import com.fx.migration.mapping.LegacyMapper
import com.fx.migration.mapping.Mapped
import com.fx.migration.target.ReportRow
import com.fx.migration.target.TargetWriter
import com.fx.migration.target.UserRow
import io.hypersistence.tsid.TSID
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime

/**
 * 레거시 컬렉션을 순서대로 옮기고 건수를 검증한다.
 *
 * 1. 공지 → `notice` · `notice_content`
 * 2. FCM 토큰 → `fcm_token` · `fcm_token_subscription`
 * 3. 관리자 계정 · 팁 · 이미지
 * 4. 문의 (토큰을 먼저 옮겨야 `fcm_token_id` 를 찾을 수 있다)
 *
 * 컬렉션을 [MigrationProperties.chunkSize] 개씩 읽어 chunk 마다 트랜잭션 하나로 쓴다.
 * 중간에 실패하면 이미 쓴 chunk 는 남으므로, 대상 DB 를 비우고 처음부터 다시 실행한다 (빈 테이블에만 이관한다).
 */
@Service
class MigrationService(
    private val legacyMongoReader: LegacyMongoReader,
    private val targetWriter: TargetWriter,
    transactionManager: PlatformTransactionManager,
    private val properties: MigrationProperties,
) {

    private val log = LoggerFactory.getLogger(MigrationService::class.java)
    private val transaction = TransactionTemplate(transactionManager)
    private val newId: () -> Long = { TSID.fast().toLong() }

    fun migrate(): MigrationReport {
        val nonEmptyTables = targetWriter.nonEmptyTables()
        check(nonEmptyTables.isEmpty()) { "이관 대상 테이블에 이미 데이터가 있습니다: $nonEmptyTables. 빈 DB 에서 실행하세요." }
        val topics = targetWriter.loadTopics()
        check(topics.isNotEmpty()) { "topic 시드가 없습니다. Flyway 마이그레이션을 확인하세요." }

        val mapper = LegacyMapper(topics, LocalDateTime.now(properties.legacyZone), newId)
        val report = MigrationReport()

        val notices = migrate(report, LegacyCollections.NOTICE, mapper::notice) { targetWriter.insertNotices(it) }

        var subscriptions = 0L
        val tokens = migrate(report, LegacyCollections.FCM_TOKEN, mapper::fcmToken) { rows ->
            targetWriter.insertFcmTokens(rows)
            subscriptions += rows.sumOf { it.subscriptions.size }
        }

        val users = migrate(report, LegacyCollections.USER, mapper::user, rejectDuplicateUser()) { targetWriter.insertUsers(it) }
        val tips = migrate(report, LegacyCollections.TIP, mapper::tip) { targetWriter.insertTips(it) }
        val images = migrate(report, LegacyCollections.IMAGE, mapper::image) { targetWriter.insertImages(it) }

        var inactiveTokens = 0L
        val reports = migrate(report, LegacyCollections.REPORT, mapper::report) { rows ->
            inactiveTokens += insertReports(rows, report.collections.last())
        }

        LegacyCollections.EXCLUDED.forEach { report.excluded[it] = legacyMongoReader.count(it) }
        report.tableChecks += listOf(
            check("notice", notices.migrated),
            check("notice_content", notices.migrated),
            check("fcm_token", tokens.migrated + inactiveTokens),
            check("fcm_token_subscription", subscriptions),
            check("users", users.migrated),
            check("tip", tips.migrated),
            check("image", images.migrated),
            check("report", reports.migrated),
        )

        log.info("\n{}", report.format())
        return report
    }

    /**
     * @param reject 앞서 옮긴 행과 충돌하는 행이면 건너뛸 사유
     */
    private fun <T> migrate(
        report: MigrationReport,
        collection: String,
        map: (LegacyDocument) -> Mapped<T>,
        reject: (T) -> String? = { null },
        write: (List<T>) -> Unit,
    ): CollectionResult {
        val result = CollectionResult(collection, legacyMongoReader.count(collection)).also { report.collections += it }

        legacyMongoReader.forEachChunk(collection) { documents ->
            val rows = documents.mapNotNull { document ->
                when (val mapped = map(document)) {
                    is Mapped.Skipped -> null.also { result.skip(mapped.reason) }
                    is Mapped.Row -> {
                        val rejected = reject(mapped.row)
                        if (rejected != null) {
                            null.also { result.skip(rejected) }
                        } else {
                            mapped.notes.forEach(result::note)
                            mapped.row
                        }
                    }
                }
            }
            transaction.executeWithoutResult { write(rows) }
            result.migrated(rows.size)
            log.info("{} : {} / {}", collection, result.migrated + result.skipped.values.sum(), result.source)
        }
        return result
    }

    /** MySQL 의 이메일 · 닉네임 유니크 제약은 대소문자를 구분하지 않으므로 같은 기준으로 먼저 걸러 낸다. */
    private fun rejectDuplicateUser(): (UserRow) -> String? {
        val emails = HashSet<String>()
        val nicknames = HashSet<String>()
        return { user ->
            val email = user.email.lowercase()
            val nickname = user.nickname.lowercase()
            when {
                email in emails -> "이메일 중복"
                nickname in nicknames -> "닉네임 중복"
                else -> null.also {
                    emails += email
                    nicknames += nickname
                }
            }
        }
    }

    /** 토큰이 남아 있지 않은 문의는 문의를 잃지 않도록 그 토큰을 비활성 상태로 만들어 연결한다. 만든 토큰 수를 돌려준다. */
    private fun insertReports(rows: List<ReportRow>, result: CollectionResult): Int {
        val tokenIds = targetWriter.findTokenIds(rows.map { it.token }.toSet()).toMutableMap()
        val missingTokens = rows.filter { it.token !in tokenIds }
            .onEach { result.note("토큰이 없어 비활성 토큰을 만들어 연결") }
            .groupBy { it.token }
            .mapValues { (_, reports) -> reports.minOf { it.createdAt } }

        tokenIds += targetWriter.insertInactiveTokens(missingTokens, newId)
        targetWriter.insertReports(rows, tokenIds)
        return missingTokens.size
    }

    private fun check(table: String, expected: Long): TableCheck =
        TableCheck(table, expected, targetWriter.count(table))

}
