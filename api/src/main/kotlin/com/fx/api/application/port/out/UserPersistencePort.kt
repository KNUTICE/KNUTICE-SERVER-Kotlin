package com.fx.api.application.port.out

import com.fx.api.domain.User

interface UserPersistencePort {

    fun save(user: User): User

    fun existsByEmail(email: String): Boolean

    fun existsByNickname(nickname: String): Boolean

    fun findByEmail(email: String): User?

}