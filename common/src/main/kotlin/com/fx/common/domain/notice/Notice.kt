package com.fx.common.domain.notice

import com.fx.common.domain.catalog.TopicView
import com.fx.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate
import java.time.LocalDateTime

/** 공지 제목 최대 길이 — `title` 컬럼 길이와 같아야 한다. */
const val NOTICE_TITLE_MAX_LENGTH = 500

/** 작성 부서 최대 길이 — `department` 컬럼 길이와 같아야 한다. */
const val NOTICE_DEPARTMENT_MAX_LENGTH = 100

/** URL 최대 길이 — `content_url` · `content_image_url` 컬럼 길이와 같아야 한다. */
const val NOTICE_URL_MAX_LENGTH = 1000

/**
 * 학교 게시판 공지. 본문 · AI 요약은 목록 조회가 읽지 않도록 [NoticeContent] 에 따로 둔다.
 *
 * - [nttId] 는 학교 게시판 게시글 번호이자 API 식별자 · 커서다. TSID `id` 는 외부에 내보내지 않는다.
 * - 토픽은 `topic_code` · `topic_name` 을 복제 저장해 JOIN 없이 v1 · v2 응답을 만든다.
 * - [notificationStatus] · [summaryStatus] 로 배치가 중단된 지점부터 이어서 처리한다.
 */
@Entity
@Table(
    name = "notice",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_notice_ntt_id", columnNames = ["ntt_id"]),
    ],
    indexes = [
        // 토픽별 목록 : `WHERE topic_code = ? AND ntt_id < ? ORDER BY ntt_id DESC`
        Index(name = "idx_notice_topic_code_ntt_id", columnList = "topic_code, ntt_id"),
        // 발송 대상 : `WHERE notification_status = 'PENDING'`
        Index(name = "idx_notice_notification_status", columnList = "notification_status"),
        // 요약 대상 : `WHERE summary_status = 'PENDING'`
        Index(name = "idx_notice_summary_status", columnList = "summary_status"),
    ],
)
class Notice(
    nttId: Long,
    topic: TopicView,
    title: String,
    department: String,
    contentUrl: String,
    contentImageUrl: String?,
    registrationDate: LocalDate,
    isAttachment: Boolean,
    notificationStatus: NotificationStatus,
    summaryStatus: SummaryStatus,
) : BaseEntity() {

    @Column(name = "ntt_id", nullable = false, updatable = false, comment = "학교 게시판 게시글 번호 (API 식별자)")
    val nttId: Long = nttId

    @Column(name = "topic_code", nullable = false, comment = "토픽 코드 (v2 topicId)")
    var topicCode: Int = topic.code
        protected set

    @Column(name = "topic_name", nullable = false, length = 100, comment = "토픽 이름 (v1 topic)")
    var topicName: String = topic.name
        protected set

    @Column(name = "title", nullable = false, length = NOTICE_TITLE_MAX_LENGTH, comment = "제목")
    var title: String = title
        protected set

    @Column(name = "department", nullable = false, length = NOTICE_DEPARTMENT_MAX_LENGTH, comment = "작성 부서")
    var department: String = department
        protected set

    @Column(name = "content_url", nullable = false, length = NOTICE_URL_MAX_LENGTH, comment = "원문 URL")
    var contentUrl: String = contentUrl
        protected set

    @Column(name = "content_image_url", nullable = true, length = NOTICE_URL_MAX_LENGTH, comment = "본문 첫 이미지 (알림 이미지)")
    var contentImageUrl: String? = contentImageUrl
        protected set

    @Column(name = "registration_date", nullable = false, comment = "게시일")
    var registrationDate: LocalDate = registrationDate
        protected set

    @Column(name = "is_attachment", nullable = false, comment = "첨부파일 여부")
    var isAttachment: Boolean = isAttachment
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_status", nullable = false, length = 20, comment = "PENDING / SENT / SKIPPED")
    var notificationStatus: NotificationStatus = notificationStatus
        protected set

    @Column(name = "notified_at", nullable = true, comment = "알림 발송 완료 시각")
    var notifiedAt: LocalDateTime? = null
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_status", nullable = false, length = 20, comment = "PENDING / COMPLETED / FAILED / SKIPPED")
    var summaryStatus: SummaryStatus = summaryStatus
        protected set

    @Column(name = "summary_attempt_count", nullable = false, comment = "AI 요약 시도 횟수")
    var summaryAttemptCount: Int = 0
        protected set

    /** 목록 응답의 `isContentSummary`. */
    val hasSummary: Boolean
        get() = summaryStatus == SummaryStatus.COMPLETED

    /** 관리자 수정. 본문 · 요약은 [NoticeContent] 에서 바꾼다. */
    fun update(
        topic: TopicView,
        title: String,
        department: String,
        contentUrl: String,
        contentImageUrl: String?,
        registrationDate: LocalDate,
        isAttachment: Boolean,
    ) {
        this.topicCode = topic.code
        this.topicName = topic.name
        this.title = title
        this.department = department
        this.contentUrl = contentUrl
        this.contentImageUrl = contentImageUrl
        this.registrationDate = registrationDate
        this.isAttachment = isAttachment
    }

    /** 요약이 생기면 [SummaryStatus.COMPLETED], 요약이 지워지면 [SummaryStatus.SKIPPED] 로 바꾼다. */
    fun summaryChanged(hasSummary: Boolean) {
        summaryStatus = when {
            hasSummary -> SummaryStatus.COMPLETED
            summaryStatus == SummaryStatus.COMPLETED -> SummaryStatus.SKIPPED
            else -> summaryStatus
        }
    }

    fun markNotified(notifiedAt: LocalDateTime) {
        this.notificationStatus = NotificationStatus.SENT
        this.notifiedAt = notifiedAt
    }

    /** 요약 실패를 기록한다. [maxAttempts] 에 도달하면 더 시도하지 않는다. */
    fun failSummary(maxAttempts: Int) {
        summaryAttemptCount += 1
        if (summaryAttemptCount >= maxAttempts) {
            summaryStatus = SummaryStatus.FAILED
        }
    }

    /** 요약할 본문이 없어 요약하지 않는다. */
    fun skipSummary() {
        summaryStatus = SummaryStatus.SKIPPED
    }

    companion object {

        /** 크롤링으로 새로 들어온 공지. 알림 · 요약 모두 대기 상태로 시작한다. */
        fun crawled(
            nttId: Long,
            topic: TopicView,
            title: String,
            department: String,
            contentUrl: String,
            contentImageUrl: String?,
            registrationDate: LocalDate,
            isAttachment: Boolean,
        ): Notice =
            Notice(
                nttId, topic, title, department, contentUrl, contentImageUrl, registrationDate, isAttachment,
                notificationStatus = NotificationStatus.PENDING,
                summaryStatus = SummaryStatus.PENDING,
            )

        /** 관리자가 직접 등록한 공지. 알림을 보내지 않고, 요약은 입력이 있을 때만 있다. */
        fun registeredByAdmin(
            nttId: Long,
            topic: TopicView,
            title: String,
            department: String,
            contentUrl: String,
            contentImageUrl: String?,
            registrationDate: LocalDate,
            isAttachment: Boolean,
            hasSummary: Boolean,
        ): Notice =
            Notice(
                nttId, topic, title, department, contentUrl, contentImageUrl, registrationDate, isAttachment,
                notificationStatus = NotificationStatus.SKIPPED,
                summaryStatus = if (hasSummary) SummaryStatus.COMPLETED else SummaryStatus.SKIPPED,
            )

    }

}
