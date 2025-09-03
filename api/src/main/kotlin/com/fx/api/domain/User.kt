package com.fx.api.domain

import com.fx.api.application.port.`in`.dto.UserSignUpCommand
import java.time.LocalDateTime

data class User(
    val id: String? = null,
    val email: String,
    val password: String,
    val nickname: String,
    val role: UserRole,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {

    companion object {
        fun createUser(signUpCommand: UserSignUpCommand): User {
            return User(
                email = signUpCommand.email,
                password = signUpCommand.password,
                nickname = signUpCommand.nickname,
                role = UserRole.USER
            )
        }
    }

}