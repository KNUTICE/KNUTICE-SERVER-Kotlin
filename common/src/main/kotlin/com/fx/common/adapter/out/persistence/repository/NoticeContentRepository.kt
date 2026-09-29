package com.fx.common.adapter.out.persistence.repository

import com.fx.common.domain.notice.NoticeContent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface NoticeContentRepository : JpaRepository<NoticeContent, Long> {

    fun findByNoticeId(noticeId: Long): NoticeContent?

    fun findAllByNoticeIdIn(noticeIds: Collection<Long>): List<NoticeContent>

    @Modifying
    @Query("DELETE FROM NoticeContent c WHERE c.noticeId = :noticeId")
    fun deleteByNoticeId(noticeId: Long): Int

}
