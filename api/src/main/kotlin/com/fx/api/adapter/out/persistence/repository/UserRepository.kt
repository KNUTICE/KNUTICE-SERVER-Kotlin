package com.fx.api.adapter.out.persistence.repository

import com.fx.api.domain.User
import org.springframework.data.jpa.repository.JpaRepository

/** 이메일 · 닉네임은 대소문자를 무시하고 비교한다. 유니크 인덱스도 `UPPER(...)` 로 같은 기준이다. */
interface UserRepository : JpaRepository<User, Long> {

    fun existsByEmailIgnoreCase(email: String): Boolean

    fun existsByNicknameIgnoreCase(nickname: String): Boolean

    fun findByEmailIgnoreCase(email: String): User?

}
