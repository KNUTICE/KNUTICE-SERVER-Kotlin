package com.fx.api.adapter.out.persistence.repository;

import com.fx.api.adapter.out.persistence.document.UserDocument
import org.springframework.data.mongodb.repository.MongoRepository

interface UserMongoRepository : MongoRepository<UserDocument, String> {

    fun existsByEmail(email: String): Boolean

    fun existsByNickname(nickname: String): Boolean

    fun findByEmail(email: String): UserDocument?

}
