package com.fx.global.adapter.out.persistence.base

import com.querydsl.core.annotations.QueryEntity
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@QueryEntity
open class MongoBaseDocument {

    @CreatedDate
    protected var createdAt: LocalDateTime? = null

    @LastModifiedDate
    protected var updatedAt: LocalDateTime? = null

}