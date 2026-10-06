package com.fx.api.application.port.out.user

import com.fx.api.domain.User

interface UserPersistencePort {

    fun save(user: User): User

    fun existsByEmail(email: String): Boolean

    fun existsByNickname(nickname: String): Boolean

    fun findByEmail(email: String): User?

    /** @throws com.fx.api.exception.UserException 없는 사용자 (USER_NOT_FOUND) */
    fun getById(userId: Long): User

}