package com.fx.api.domain

import com.fx.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

/** 이메일 최대 길이 — `email` 컬럼 길이와 같아야 한다. */
const val USER_EMAIL_MAX_LENGTH = 200

/** 닉네임 최대 길이 — `nickname` 컬럼 길이와 같아야 한다. */
const val USER_NICKNAME_MAX_LENGTH = 30

/**
 * 관리자 화면 로그인 계정. 테이블명 `user` 는 SQL 예약어라 `users` 를 쓴다.
 * 이메일 · 닉네임은 대소문자를 무시하고 유일하다. JPA 로 나타낼 수 없는 식 인덱스(`UPPER(email)`)라 Flyway 스크립트에만 있다.
 */
@Entity
@Table(name = "users")
class User(
    email: String,
    encodedPassword: String,
    nickname: String,
    role: UserRole,
) : BaseEntity() {

    @Column(name = "email", nullable = false, updatable = false, length = USER_EMAIL_MAX_LENGTH, comment = "이메일 (로그인 ID, 대소문자 무시)")
    val email: String = email

    @Column(name = "password", nullable = false, length = 100, comment = "비밀번호 해시")
    var password: String = encodedPassword
        protected set

    @Column(name = "nickname", nullable = false, length = USER_NICKNAME_MAX_LENGTH, comment = "닉네임 (대소문자 무시)")
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
