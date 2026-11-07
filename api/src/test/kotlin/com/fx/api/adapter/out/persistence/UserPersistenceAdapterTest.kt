package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.UserRepository
import com.fx.api.domain.User
import com.fx.persistence.PostgreSqlContainerConfig
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException

/** 실제 PostgreSQL 에서 이메일 · 닉네임을 대소문자 무시로 찾고, 유니크 인덱스도 같은 기준인지 확인한다. */
@DataJpaTest(properties = ["POSTGRES_URL=unused", "POSTGRES_DATABASE=unused", "POSTGRES_USERNAME=unused", "POSTGRES_PASSWORD=unused"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgreSqlContainerConfig::class, UserPersistenceAdapter::class)
class UserPersistenceAdapterTest @Autowired constructor(
    private val userPersistenceAdapter: UserPersistenceAdapter,
    private val userRepository: UserRepository,
    private val entityManager: EntityManager,
) {

    @Test
    fun `이메일 · 닉네임은 대소문자를 무시하고 찾는다`() {
        val saved = userPersistenceAdapter.save(User.signUp("Admin@Knutice.com", "hash", "Manager"))
        flushAndClear()

        assertThat(userPersistenceAdapter.findByEmail("admin@knutice.com")?.id).isEqualTo(saved.id)
        assertThat(userPersistenceAdapter.existsByEmail("ADMIN@KNUTICE.COM")).isTrue()
        assertThat(userPersistenceAdapter.existsByNickname("manager")).isTrue()
        assertThat(userPersistenceAdapter.existsByEmail("other@knutice.com")).isFalse()
    }

    @Test
    fun `대소문자만 다른 이메일은 저장할 수 없다`() {
        userRepository.saveAndFlush(User.signUp("admin@knutice.com", "hash", "first"))

        assertThatThrownBy {
            userRepository.saveAndFlush(User.signUp("ADMIN@knutice.com", "hash", "second"))
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    @Test
    fun `대소문자만 다른 닉네임은 저장할 수 없다`() {
        userRepository.saveAndFlush(User.signUp("first@knutice.com", "hash", "manager"))

        assertThatThrownBy {
            userRepository.saveAndFlush(User.signUp("second@knutice.com", "hash", "MANAGER"))
        }.isInstanceOf(DataIntegrityViolationException::class.java)
    }

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }

}
