package com.fx.common.adapter.out.persistence.repository

import com.fx.common.domain.notice.Notice
import com.fx.common.domain.notice.NotificationStatus
import com.fx.common.domain.notice.SummaryStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface NoticeRepository : JpaRepository<Notice, Long> {

    fun findByNttId(nttId: Long): Notice?

    fun existsByNttId(nttId: Long): Boolean

    fun countBySummaryStatus(summaryStatus: SummaryStatus): Long

    /** 이미 저장된 게시글 번호만 읽는다 (신규 공지 판별). */
    @Query("SELECT n.nttId FROM Notice n WHERE n.nttId IN :nttIds")
    fun findExistingNttIds(nttIds: Collection<Long>): List<Long>

    /** 발송할 공지. 같은 토픽 안에서는 최신 게시글이 먼저 온다. */
    fun findAllByNotificationStatusAndTopicCodeInOrderByNttIdDesc(
        notificationStatus: NotificationStatus,
        topicCodes: Collection<Int>,
    ): List<Notice>

    /** 발송을 마친 공지를 한 번에 표시한다. 이미 처리된 공지는 건드리지 않는다. */
    @Modifying
    @Query(
        """
        UPDATE Notice n
        SET n.notificationStatus = com.fx.common.domain.notice.NotificationStatus.SENT, n.notifiedAt = :now, n.updatedAt = :now
        WHERE n.id IN :ids AND n.notificationStatus = com.fx.common.domain.notice.NotificationStatus.PENDING
        """
    )
    fun markNotified(ids: Collection<Long>, now: LocalDateTime): Int

}
