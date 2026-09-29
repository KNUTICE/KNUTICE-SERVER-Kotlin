package com.fx.crawler.adapter.out.persistence

import com.fx.common.adapter.out.persistence.repository.NoticeContentRepository
import com.fx.common.adapter.out.persistence.repository.NoticeRepository
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.domain.notice.NOTICE_DEPARTMENT_MAX_LENGTH
import com.fx.common.domain.notice.NOTICE_TITLE_MAX_LENGTH
import com.fx.common.domain.notice.NOTICE_URL_MAX_LENGTH
import com.fx.common.domain.notice.Notice
import com.fx.common.domain.notice.NoticeContent
import com.fx.common.domain.notice.NotificationStatus
import com.fx.common.exception.NoticeException
import com.fx.common.exception.errorcode.NoticeErrorCode
import com.fx.crawler.adapter.out.persistence.repository.NoticeSummaryQueryRepository
import com.fx.crawler.application.port.out.NoticePersistencePort
import com.fx.crawler.domain.crawl.CrawledNotice
import com.fx.crawler.domain.summary.SummaryTarget
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@PersistenceAdapter
class NoticePersistenceAdapter(
    private val noticeRepository: NoticeRepository,
    private val noticeContentRepository: NoticeContentRepository,
    private val noticeSummaryQueryRepository: NoticeSummaryQueryRepository,
) : NoticePersistencePort {

    @Transactional(readOnly = true)
    override fun findExistingNttIds(nttIds: Collection<Long>): Set<Long> =
        if (nttIds.isEmpty()) emptySet() else noticeRepository.findExistingNttIds(nttIds).toSet()

    /** 제목 · 부서는 학교 원문이라 길이를 믿을 수 없으므로 컬럼 길이에 맞춰 자른다. */
    @Transactional
    override fun saveCrawled(notices: List<CrawledNotice>) {
        val saved = noticeRepository.saveAll(
            notices.map {
                Notice.crawled(
                    nttId = it.nttId,
                    topic = it.topic,
                    title = it.title.take(NOTICE_TITLE_MAX_LENGTH),
                    department = it.department.take(NOTICE_DEPARTMENT_MAX_LENGTH),
                    contentUrl = it.contentUrl,
                    contentImageUrl = it.contentImageUrl?.takeIf { url -> url.length <= NOTICE_URL_MAX_LENGTH },
                    registrationDate = it.registrationDate,
                    isAttachment = it.isAttachment,
                )
            }
        )
        val contentsByNttId = notices.associate { it.nttId to it.content?.takeIf { content -> content.isNotBlank() } }
        noticeContentRepository.saveAll(
            saved.map { NoticeContent(requireNotNull(it.id), content = contentsByNttId[it.nttId], contentSummary = null) }
        )
    }

    @Transactional(readOnly = true)
    override fun findPendingNotification(topicCodes: Collection<Int>): List<Notice> =
        noticeRepository.findAllByNotificationStatusAndTopicCodeInOrderByNttIdDesc(NotificationStatus.PENDING, topicCodes)

    @Transactional
    override fun markNotified(noticeIds: Collection<Long>, now: LocalDateTime): Int =
        noticeRepository.markNotified(noticeIds, now)

    @Transactional(readOnly = true)
    override fun findSummaryTargets(topicCodes: Collection<Int>, afterNoticeId: Long?, size: Int): List<SummaryTarget> =
        noticeSummaryQueryRepository.findSummaryTargets(topicCodes, afterNoticeId, size)

    @Transactional(readOnly = true)
    override fun findAllByIds(noticeIds: Collection<Long>): List<Notice> =
        noticeRepository.findAllById(noticeIds)

    @Transactional(readOnly = true)
    override fun findContents(noticeIds: Collection<Long>): List<NoticeContent> =
        noticeContentRepository.findAllByNoticeIdIn(noticeIds)

    @Transactional(readOnly = true)
    override fun getByNttId(nttId: Long): Notice =
        noticeRepository.findByNttId(nttId) ?: throw NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND)

}
