package com.fx.api.application.service.question

import com.fx.api.application.port.`in`.question.NoticeQuestionUseCase
import com.fx.api.application.port.`in`.question.dto.FinishQuestionCommand
import com.fx.api.application.port.`in`.question.dto.PrepareQuestionCommand
import com.fx.api.application.port.`in`.question.dto.QuestionAsker
import com.fx.api.application.port.out.fcmtoken.FcmTokenPersistencePort
import com.fx.api.application.port.out.question.NoticeQuestionPersistencePort
import com.fx.api.application.port.out.question.QuestionEmbeddingPort
import com.fx.api.config.question.NoticeQuestionProperties
import com.fx.api.domain.question.FinishedQuestion
import com.fx.api.domain.question.NoticeAnswerPrompt
import com.fx.api.domain.question.NoticeCitations
import com.fx.api.domain.question.NoticeQuestion
import com.fx.api.domain.question.NoticeQuestionAsker
import com.fx.api.domain.question.NoticeQuestionStatus
import com.fx.api.domain.question.NoticeQuestionVerdict
import com.fx.api.domain.question.NoticeSearchRanking
import com.fx.api.domain.question.NoticeSourceInfo
import com.fx.api.domain.question.NoticeSourcePicker
import com.fx.api.domain.question.PreparedQuestion
import com.fx.api.domain.question.QuestionQuota
import com.fx.api.domain.question.QuestionWindow
import com.fx.api.exception.QuestionException
import com.fx.api.exception.errorcode.QuestionErrorCode
import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.domain.i18n.Language
import com.fx.common.exception.FcmTokenException
import com.fx.common.exception.errorcode.FcmTokenErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

/**
 * 공지 질문(RAG). 서버는 검색 · 프롬프트만 만들고, 답변은 앱 · 관리자 웹이 사용자 개인 AI 키로 받아 돌려준다.
 *
 * - 서버 무료 키는 질문 임베딩에만 쓴다 (질문 1개 = 임베딩 1회). 그래서 기기 · 관리자 · 서버 전체 하루 한도를 그 전에 확인한다.
 * - 앱 기기는 등록된 fcmToken 만 받고, 원문 대신 HMAC 해시로 센다. 근거를 못 찾은 질문은 기기 한도에 세지 않는다.
 * - prepare 는 Gemini 를 부르는 동안 DB 트랜잭션을 잡지 않도록 메서드 전체에 트랜잭션을 걸지 않는다.
 * - 동시에 들어온 질문은 한도를 조금 넘길 수 있다 (잠그지 않는다).
 */
@Service
class NoticeQuestionService(
    private val properties: NoticeQuestionProperties,
    private val questionEmbeddingPort: QuestionEmbeddingPort,
    private val noticeQuestionPersistencePort: NoticeQuestionPersistencePort,
    private val fcmTokenPersistencePort: FcmTokenPersistencePort,
    private val catalogQueryUseCase: CatalogQueryUseCase,
    private val signer: NoticeQuestionSigner,
    private val clock: Clock,
) : NoticeQuestionUseCase {

    override fun prepare(command: PrepareQuestionCommand): PreparedQuestion {
        val startedAt = System.nanoTime()
        val asker = identify(command.asker)
        val now = now()
        val windowStart = QuestionWindow.start(now, properties.resetHour)
        if (noticeQuestionPersistencePort.countSince(windowStart) >= properties.totalDailyLimit) {
            throw QuestionException(QuestionErrorCode.SERVICE_DAILY_LIMIT_EXCEEDED)
        }
        val used = countUsed(asker, windowStart)
        if (used >= limitOf(asker)) {
            throw QuestionException(QuestionErrorCode.DAILY_LIMIT_EXCEEDED)
        }

        val ranking = NoticeSearchRanking(
            decayPerYear = (command.decayPerYear ?: properties.decayPerYear).coerceIn(0.0, MAX_PENALTY),
            titlePenalty = (command.titlePenalty ?: properties.titlePenalty).coerceIn(0.0, MAX_PENALTY),
        )
        val topNotices = (command.topNotices ?: properties.topNotices).coerceIn(1, MAX_TOP_NOTICES)

        val embedding = questionEmbeddingPort.embed(command.question)
        val hits = noticeQuestionPersistencePort.searchChunks(
            embedding = embedding,
            embeddingModel = questionEmbeddingPort.modelTag,
            ranking = ranking,
            today = now.toLocalDate(),
            limit = properties.candidateChunks,
        )
        val sources = NoticeSourcePicker.pick(hits, topNotices, properties.chunksPerNotice)
        val topSimilarity = sources.maxOfOrNull {
            it.similarity
        }
        val found = topSimilarity != null && topSimilarity >= properties.minSimilarity
        val status = if (found) NoticeQuestionStatus.PREPARED else NoticeQuestionStatus.NOT_FOUND
        val elapsedMs = ((System.nanoTime() - startedAt) / NANOS_PER_MILLI).toInt()

        val saved = noticeQuestionPersistencePort.save(
            NoticeQuestion(
                askedBy = asker.askedBy,
                askerKey = asker.askerKey,
                question = command.question,
                status = status,
                sourceNttIds = sources.map {
                    it.nttId
                },
                topSimilarity = topSimilarity,
                decayPerYear = ranking.decayPerYear,
                titlePenalty = ranking.titlePenalty,
                elapsedMs = elapsedMs,
            )
        )
        val questionId = requireNotNull(saved.id)
        val expiresAt = clock.instant().plus(properties.finishTokenTtl)
        // 앱은 근거를 못 찾은 질문을 세지 않는다
        val counted = found || asker.askedBy == NoticeQuestionAsker.ADMIN

        return PreparedQuestion(
            questionId = questionId,
            status = status,
            sources = sources,
            messages = if (found) NoticeAnswerPrompt.messages(command.question, sources, now) else null,
            finishToken = if (found) signer.finishToken(questionId, asker.askerKey, expiresAt) else null,
            finishExpiresAt = if (found) expiresAt else null,
            retrieved = hits.take(properties.retrievedLimit),
            ranking = ranking,
            minSimilarity = properties.minSimilarity,
            elapsedMs = elapsedMs,
            quota = QuestionQuota(
                used = used + if (counted) 1 else 0,
                limit = limitOf(asker),
                resetsAt = QuestionWindow.nextReset(now, properties.resetHour),
            ),
        )
    }

    @Transactional
    override fun finish(command: FinishQuestionCommand): FinishedQuestion {
        val asker = identify(command.asker)
        val question = ownedQuestion(asker, command.questionId)
        if (!signer.verifyFinishToken(command.finishToken, command.questionId, asker.askerKey, clock.instant())) {
            throw QuestionException(QuestionErrorCode.FINISH_TOKEN_INVALID)
        }
        val sourceNttIds = question.sourceNttIds
        val (answer, cited) = NoticeCitations.clean(command.answer.trim().take(properties.answerMaxLength), sourceNttIds.size)
        if (answer.isBlank()) {
            throw QuestionException(QuestionErrorCode.ANSWER_EMPTY)
        }
        val finished = question.finish(
            answer = answer,
            provider = command.provider,
            model = command.model.trim(),
            inputTokens = command.inputTokens,
            outputTokens = command.outputTokens,
            answeredAt = now(),
        )
        if (!finished) {
            throw QuestionException(QuestionErrorCode.ALREADY_FINISHED)
        }
        return FinishedQuestion(
            questionId = command.questionId,
            answer = answer,
            citedNumbers = cited,
            sources = citedSources(sourceNttIds, cited),
        )
    }

    override fun getQuota(asker: QuestionAsker): QuestionQuota {
        val identified = identify(asker)
        val now = now()
        return QuestionQuota(
            used = countUsed(identified, QuestionWindow.start(now, properties.resetHour)),
            limit = limitOf(identified),
            resetsAt = QuestionWindow.nextReset(now, properties.resetHour),
        )
    }

    @Transactional
    override fun giveFeedback(asker: QuestionAsker, questionId: Long, verdict: NoticeQuestionVerdict, memo: String?): Boolean {
        ownedQuestion(identify(asker), questionId).giveFeedback(verdict, memo)
        return true
    }

    /** 앱 기기는 등록된 fcmToken 인지 확인하고 HMAC 해시로 바꾼다 */
    private fun identify(asker: QuestionAsker): IdentifiedAsker =
        when (asker) {
            is QuestionAsker.Admin -> IdentifiedAsker(NoticeQuestionAsker.ADMIN, asker.userId)
            is QuestionAsker.App -> {
                if (asker.fcmToken.isBlank() || !fcmTokenPersistencePort.existsByToken(asker.fcmToken)) {
                    throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)
                }
                IdentifiedAsker(NoticeQuestionAsker.APP, signer.deviceKey(asker.fcmToken))
            }
        }

    /** 다른 사람의 질문은 없는 것으로 다룬다 (있는지 여부도 알리지 않는다) */
    private fun ownedQuestion(asker: IdentifiedAsker, questionId: Long): NoticeQuestion {
        val question = noticeQuestionPersistencePort.getQuestion(questionId)
        if (!question.isAskedBy(asker.askedBy, asker.askerKey)) {
            throw QuestionException(QuestionErrorCode.QUESTION_NOT_FOUND)
        }
        return question
    }

    private fun countUsed(asker: IdentifiedAsker, windowStart: LocalDateTime): Long =
        when (asker.askedBy) {
            NoticeQuestionAsker.ADMIN -> noticeQuestionPersistencePort.countByAskedBySince(NoticeQuestionAsker.ADMIN, windowStart)
            NoticeQuestionAsker.APP -> noticeQuestionPersistencePort.countByAskerSince(
                NoticeQuestionAsker.APP, asker.askerKey, windowStart, excludedStatus = NoticeQuestionStatus.NOT_FOUND,
            )
        }

    private fun limitOf(asker: IdentifiedAsker): Int =
        when (asker.askedBy) {
            NoticeQuestionAsker.ADMIN -> properties.adminDailyLimit
            NoticeQuestionAsker.APP -> properties.appDailyLimit
        }

    /** 답변이 쓴 근거 번호의 공지. 그 사이 지워진 공지는 빠진다 */
    private fun citedSources(sourceNttIds: List<Long>, cited: List<Int>): List<NoticeSourceInfo> {
        val citedNttIds = cited.associateWith {
            sourceNttIds[it - 1]
        }
        val notices = noticeQuestionPersistencePort.findNotices(citedNttIds.values).associateBy {
            it.nttId
        }
        val catalog = catalogQueryUseCase.getTopicCatalog()
        return citedNttIds.mapNotNull { (n, nttId) ->
            notices[nttId]?.let { notice ->
                NoticeSourceInfo(
                    n = n,
                    nttId = nttId,
                    title = notice.title,
                    boardName = catalog.findByCode(notice.topicCode)?.displayName?.resolve(Language.KO),
                    registrationDate = notice.registrationDate,
                    contentUrl = notice.contentUrl,
                )
            }
        }
    }

    private fun now(): LocalDateTime =
        LocalDateTime.now(clock.withZone(QuestionWindow.ZONE))

    private data class IdentifiedAsker(
        val askedBy: NoticeQuestionAsker,
        val askerKey: String,
    )

    companion object {
        /** 요청으로 바꿀 수 있는 감점의 상한 */
        private const val MAX_PENALTY = 0.2
        private const val MAX_TOP_NOTICES = 10
        private const val NANOS_PER_MILLI = 1_000_000
    }

}
