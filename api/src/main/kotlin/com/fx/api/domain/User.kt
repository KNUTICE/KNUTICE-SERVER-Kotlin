package com.fx.api.domain

import com.fx.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

/** 이메일 최대 길이 — `email` 컬럼 길이와 같아야 한다. */
const val USER_EMAIL_MAX_LENGTH = 200

/** 닉네임 최대 길이 — `nickname` 컬럼 길이와 같아야 한다. */
const val USER_NICKNAME_MAX_LENGTH = 30

/** 관리자 화면 로그인 계정. 테이블명 `user` 는 SQL 예약어라 `users` 를 쓴다. */
@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_users_email", columnNames = ["email"]),
        UniqueConstraint(name = "uk_users_nickname", columnNames = ["nickname"]),
    ],
)
class User(
    email: String,
    encodedPassword: String,
    nickname: String,
    role: UserRole,
) : BaseEntity() {

    @Column(name = "email", nullable = false, updatable = false, length = USER_EMAIL_MAX_LENGTH, comment = "이메일 (로그인 ID)")
    val email: String = email

    @Column(name = "password", nullable = false, length = 100, comment = "비밀번호 해시")
    var password: String = encodedPassword
        protected set

    @Column(name = "nickname", nullable = false, length = USER_NICKNAME_MAX_LENGTH, comment = "닉네임")
    var nickname: String = nickname
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20, comment = "ADMIN / USER")
    var role: UserRole = role
        protected set

    companion object {

        fun signUp(email: String, encodedPassword: String, nickname: String): User =
            User(email, encodedPassword, nickname, UserRole.USER)

    }

}
