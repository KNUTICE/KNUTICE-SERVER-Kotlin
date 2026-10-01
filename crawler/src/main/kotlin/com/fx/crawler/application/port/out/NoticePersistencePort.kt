package com.fx.crawler.application.port.out

import com.fx.common.domain.notice.Notice
import com.fx.common.domain.notice.NoticeContent
import com.fx.common.exception.NoticeException
import com.fx.crawler.domain.crawl.CrawledNotice
import com.fx.crawler.domain.summary.SummaryTarget
import java.time.LocalDateTime

interface NoticePersistencePort {

    /** [nttIds] 중 이미 저장된 게시글 번호. */
    fun findExistingNttIds(nttIds: Collection<Long>): Set<Long>

    /** 크롤링한 새 공지를 알림 · 요약 대기 상태로 저장한다. */
    fun saveCrawled(notices: List<CrawledNotice>)

    /** 발송 대기 중인 공지. 토픽 안에서는 최신 게시글이 먼저 온다. */
    fun findPendingNotification(topicCodes: Collection<Int>): List<Notice>

    fun markNotified(noticeIds: Collection<Long>, now: LocalDateTime): Int

    /**
     * 요약 대기 중인 공지를 id 순으로 [afterNoticeId] 다음부터 [size] 개.
     * 요약에 실패한 적이 있는 공지는 마지막 시도가 [retryBefore] 보다 전일 때만 포함한다.
     */
    fun findSummaryTargets(topicCodes: Collection<Int>, retryBefore: LocalDateTime, afterNoticeId: Long?, size: Int): List<SummaryTarget>

    fun findAllByIds(noticeIds: Collection<Long>): List<Notice>

    fun findContents(noticeIds: Collection<Long>): List<NoticeContent>

    /** @throws NoticeException 공지가 없을 때 */
    fun getByNttId(nttId: Long): Notice

}
