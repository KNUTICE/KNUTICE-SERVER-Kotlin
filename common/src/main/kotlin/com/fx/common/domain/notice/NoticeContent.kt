package com.fx.common.domain.notice

import com.fx.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

/**
 * 공지 본문과 AI 요약. 본문은 어떤 API 응답에도 나가지 않고 요약 입력으로만 쓰며,
 * 요약은 요약 조회 API 에서만 쓰므로 [Notice] 와 분리해 목록 조회가 읽지 않게 한다.
 */
@Entity
@Table(
    name = "notice_content",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_notice_content_notice_id", columnNames = ["notice_id"]),
    ],
)
class NoticeContent(
    noticeId: Long,
    content: String?,
    contentSummary: String?,
) : BaseEntity() {

    @Column(name = "notice_id", nullable = false, updatable = false, comment = "notice.id")
    val noticeId: Long = noticeId

    /** TEXT(64KB)는 한글 약 2만 자가 한계라 MEDIUMTEXT 를 쓴다. */
    @Column(name = "content", nullable = true, columnDefinition = "MEDIUMTEXT", comment = "본문 텍스트 (AI 요약 입력)")
    var content: String? = content
        protected set

    @Column(name = "content_summary", nullable = true, columnDefinition = "TEXT", comment = "AI 요약")
    var contentSummary: String? = contentSummary
        protected set

    fun changeContent(content: String?) {
        this.content = content
    }

    fun changeSummary(contentSummary: String?) {
        this.contentSummary = contentSummary?.takeIf {
            it.isNotBlank()
        }
    }

}
