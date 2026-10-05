package com.fx.common.adapter.out.persistence.repository

import com.fx.common.domain.topic.Topic
import org.springframework.data.jpa.repository.JpaRepository

interface TopicRepository : JpaRepository<Topic, Long> {

    fun findAllByDeletedAtIsNull(): List<Topic>

}
