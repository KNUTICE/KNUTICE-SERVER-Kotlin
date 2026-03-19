package com.fx.api.application.port.out

import com.fx.api.domain.User

interface UserPersistencePort {

    suspend fun save(user: User): User

    suspend fun existsByEmail(email: String): Boolean

    suspend fun existsByNickname(nickname: String): Boolean

    suspend fun findByEmail(email: String): User?

}