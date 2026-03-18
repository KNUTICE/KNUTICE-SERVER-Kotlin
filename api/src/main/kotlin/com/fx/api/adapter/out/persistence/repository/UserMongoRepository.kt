package com.fx.api.adapter.out.persistence.repository;

import com.fx.api.adapter.out.persistence.document.UserDocument
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface UserMongoRepository : CoroutineCrudRepository<UserDocument, String> {

    suspend fun existsByEmail(email: String): Boolean

    suspend fun existsByNickname(nickname: String): Boolean

    suspend fun findByEmail(email: String): UserDocument?

}
