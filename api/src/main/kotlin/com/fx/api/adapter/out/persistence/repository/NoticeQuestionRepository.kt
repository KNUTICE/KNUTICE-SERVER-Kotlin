package com.fx.api.adapter.out.persistence.repository

import com.fx.api.domain.question.NoticeQuestion
import com.fx.api.domain.question.NoticeQuestionAsker
import com.fx.api.domain.question.NoticeQuestionStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface NoticeQuestionRepository : JpaRepository<NoticeQuestion, Long> {

    fun countByCreatedAtGreaterThanEqual(createdAt: LocalDateTime): Long

    fun countByAskedByAndCreatedAtGreaterThanEqual(askedBy: NoticeQuestionAsker, createdAt: LocalDateTime): Long

    fun countByAskedByAndAskerKeyAndStatusNotAndCreatedAtGreaterThanEqual(
        askedBy: NoticeQuestionAsker,
        askerKey: String,
        status: NoticeQuestionStatus,
        createdAt: LocalDateTime,
    ): Long

}
