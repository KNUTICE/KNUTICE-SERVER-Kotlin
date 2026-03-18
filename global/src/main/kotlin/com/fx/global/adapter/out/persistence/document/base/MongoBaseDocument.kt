package com.fx.global.adapter.out.persistence.document.base

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

open class MongoBaseDocument {

    @CreatedDate
    protected var createdAt: LocalDateTime? = null

    @LastModifiedDate
    protected var updatedAt: LocalDateTime? = null

}