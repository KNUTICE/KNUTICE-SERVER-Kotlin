package com.fx.common.domain.topic

import com.fx.common.domain.TopicType
import com.fx.common.domain.i18n.LocalizedText
import com.fx.persistence.BaseEntity
import jakarta.persistence.AttributeOverride
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

/** 토픽 이름 최대 길이 — `name` 컬럼 길이와 같아야 한다. */
const val TOPIC_NAME_MAX_LENGTH = 100

/** 토픽 표시명 최대 길이 — `display_name_*` 컬럼 길이와 같아야 한다. */
const val TOPIC_DISPLAY_NAME_MAX_LENGTH = 100

/** 크롤링 도메인 최대 길이 — `root_domain` 컬럼 길이와 같아야 한다. */
const val TOPIC_ROOT_DOMAIN_MAX_LENGTH = 200

/** 크롤링 게시판 경로 최대 길이 — `bbs_path` 컬럼 길이와 같아야 한다. */
const val TOPIC_BBS_PATH_MAX_LENGTH = 500

/** v1 API 의 토픽 식별자 형식 (예: `GENERAL_NEWS`, `COMPUTER_SOFTWARE`). */
private val TOPIC_NAME_PATTERN = Regex("^[A-Z][A-Z0-9_]*$")

/**
 * 크롤링 · 구독 단위가 되는 토픽 (공지 게시판 · 학과 게시판 · 학식).
 *
 * - [code] 는 v2 API 의 `topicId`, [name] 은 v1 API 의 토픽 식별자다. 공지 · 구독에 복제 저장되고
 *   앱이 식별자로 쓰므로 생성 후 바꿀 수 없다.
 * - 단과대([collegeId])는 학과(MAJOR) 토픽에만 둔다.
 * - 삭제는 논리 삭제다. 과거 공지 · 구독이 참조하고, 구버전 앱이 해당 토픽을 알고 있을 수 있다.
 */
@Entity
@Table(
    name = "topic",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_topic_code", columnNames = ["code"]),
        UniqueConstraint(name = "uk_topic_name", columnNames = ["name"]),
    ],
    indexes = [
        // 단과대 삭제 전 소속 토픽 확인 : `WHERE college_id = ?`
        Index(name = "idx_topic_college_id", columnList = "college_id"),
    ],
)
class Topic(
    code: Int,
    name: String,
    topicType: TopicType,
    displayName: LocalizedText,
    collegeId: Long?,
    rootDomain: String,
    bbsPath: String,
    crawlEnabled: Boolean = true,
    visible: Boolean = true,
    summaryEnabled: Boolean = true,
) : BaseEntity() {

    @Column(name = "code", nullable = false, updatable = false, comment = "토픽 코드 (v2 topicId, 생성 후 변경 불가)")
    val code: Int = code

    @Column(name = "name", nullable = false, updatable = false, length = TOPIC_NAME_MAX_LENGTH, comment = "토픽 이름 (v1 topic, 생성 후 변경 불가)")
    val name: String = name

    @Enumerated(EnumType.STRING)
    @Column(name = "topic_type", nullable = false, updatable = false, length = 20, comment = "NOTICE / MAJOR / MEAL")
    val topicType: TopicType = topicType

    @Embedded
    @AttributeOverride(name = "ko", column = Column(name = "display_name_ko", nullable = false, length = TOPIC_DISPLAY_NAME_MAX_LENGTH, comment = "표시명 (한국어)"))
    @AttributeOverride(name = "en", column = Column(name = "display_name_en", nullable = true, length = TOPIC_DISPLAY_NAME_MAX_LENGTH, comment = "표시명 (영어)"))
    @AttributeOverride(name = "ja", column = Column(name = "display_name_ja", nullable = true, length = TOPIC_DISPLAY_NAME_MAX_LENGTH, comment = "표시명 (일본어)"))
    var displayName: LocalizedText = displayName
        protected set

    @Column(name = "college_id", nullable = true, comment = "단과대 ID (MAJOR 전용)")
    var collegeId: Long? = collegeId
        protected set

    @Column(name = "root_domain", nullable = false, length = TOPIC_ROOT_DOMAIN_MAX_LENGTH, comment = "크롤링 대상 도메인")
    var rootDomain: String = rootDomain
        protected set

    @Column(name = "bbs_path", nullable = false, length = TOPIC_BBS_PATH_MAX_LENGTH, comment = "크롤링 대상 게시판 경로")
    var bbsPath: String = bbsPath
        protected set

    @Column(name = "crawl_enabled", nullable = false, comment = "크롤링 대상 여부")
    var crawlEnabled: Boolean = crawlEnabled
        protected set

    @Column(name = "visible", nullable = false, comment = "앱 노출 여부")
    var visible: Boolean = visible
        protected set

    /** 새로 크롤링한 공지를 AI 요약할지. 크롤링할 때만 보므로, 꺼 둔 동안 들어온 공지는 다시 켜도 요약하지 않는다. */
    @Column(name = "summary_enabled", nullable = false, comment = "AI 요약 여부")
    var summaryEnabled: Boolean = summaryEnabled
        protected set

    @Column(name = "deleted_at", nullable = true, comment = "삭제 시각")
    var deletedAt: LocalDateTime? = null
        protected set

    init {
        require(code > 0) {
            "토픽 코드는 양수여야 합니다."
        }
        require(TOPIC_NAME_PATTERN.matches(name)) {
            "토픽 이름은 대문자 · 숫자 · 밑줄로만 쓸 수 있습니다: $name"
        }
        requireCollegeRule(topicType, collegeId)
    }

    val isDeleted: Boolean
        get() = deletedAt != null

    /** 크롤링할 게시판 목록 URL. */
    fun noticeUrl(): String =
        rootDomain + bbsPath

    fun changeDisplayName(displayName: LocalizedText) {
        this.displayName = displayName
    }

    fun changeCollege(collegeId: Long?) {
        requireCollegeRule(topicType, collegeId)
        this.collegeId = collegeId
    }

    fun changeCrawlSource(rootDomain: String, bbsPath: String) {
        this.rootDomain = rootDomain
        this.bbsPath = bbsPath
    }

    fun changeCrawlEnabled(crawlEnabled: Boolean) {
        this.crawlEnabled = crawlEnabled
    }

    fun changeVisible(visible: Boolean) {
        this.visible = visible
    }

    fun changeSummaryEnabled(summaryEnabled: Boolean) {
        this.summaryEnabled = summaryEnabled
    }

    fun delete(deletedAt: LocalDateTime) {
        this.deletedAt = deletedAt
    }

    private fun requireCollegeRule(topicType: TopicType, collegeId: Long?) {
        if (topicType == TopicType.MAJOR) {
            require(collegeId != null) {
                "학과 토픽에는 단과대가 필요합니다."
            }
        } else {
            require(collegeId == null) {
                "단과대는 학과 토픽에만 지정할 수 있습니다."
            }
        }
    }

}
