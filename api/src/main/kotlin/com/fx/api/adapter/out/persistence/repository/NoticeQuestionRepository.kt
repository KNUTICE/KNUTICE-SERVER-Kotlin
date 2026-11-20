package com.fx.api.adapter.out.persistence.repository

import com.fx.api.domain.question.NoticeQuestion
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface NoticeQuestionRepository : JpaRepository<NoticeQuestion, Long> {

    fun countByCreatedAtGreaterThanEqual(createdAt: LocalDateTime): Long

}
