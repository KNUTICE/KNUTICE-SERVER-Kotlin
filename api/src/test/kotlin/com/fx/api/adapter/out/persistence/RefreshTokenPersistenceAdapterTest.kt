package com.fx.api.adapter.out.persistence

import com.fx.api.domain.RefreshToken
import com.fx.persistence.MySqlContainerConfig
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import java.time.Duration
import java.time.LocalDateTime

/** 실제 MySQL 에서 리프레시 토큰 세션을 저장 · 회전 · 삭제한다. */
@DataJpaTest(properties = ["MYSQL_URL=unused", "MYSQL_DATABASE=unused", "MYSQL_USERNAME=unused", "MYSQL_PASSWORD=unused"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySqlContainerConfig::class, RefreshTokenPersistenceAdapter::class)
class RefreshTokenPersistenceAdapterTest @Autowired constructor(
    private val refreshTokenPersistenceAdapter: RefreshTokenPersistenceAdapter,
    private val entityManager: EntityManager,
) {

    private val now = LocalDateTime.of(2026, 10, 15, 9, 0)
    private val lifetime = Duration.ofDays(14)

    @Test
    fun `회전한 토큰은 현재 토큰과 직전 토큰으로 각각 찾는다`() {
        val session = refreshTokenPersistenceAdapter.save(RefreshToken.issue(1L, "old", now, lifetime))
        flushAndClear()

        val found = requireNotNull(refreshTokenPersistenceAdapter.findByTokenHashForUpdate(RefreshToken.hash("old")))
        found.rotate("new", now.plusDays(1), lifetime)
        flushAndClear()

        assertThat(refreshTokenPersistenceAdapter.findByTokenHashForUpdate(RefreshToken.hash("old"))).isNull()
        assertThat(refreshTokenPersistenceAdapter.findByTokenHashForUpdate(RefreshToken.hash("new"))?.id).isEqualTo(session.id)
        val previous = requireNotNull(refreshTokenPersistenceAdapter.findByPreviousTokenHash(RefreshToken.hash("old")))
        assertThat(previous.id).isEqualTo(session.id)
        assertThat(previous.expiresAt).isEqualTo(now.plusDays(1).plus(lifetime))
    }

    @Test
    fun `만료 시각이 지난 세션만 지운다`() {
        refreshTokenPersistenceAdapter.save(RefreshToken.issue(1L, "expired", now.minus(lifetime), lifetime))
        refreshTokenPersistenceAdapter.save(RefreshToken.issue(1L, "active", now.minus(lifetime).plusSeconds(1), lifetime))
        flushAndClear()

        refreshTokenPersistenceAdapter.deleteExpired(now)

        assertThat(refreshTokenPersistenceAdapter.findByTokenHashForUpdate(RefreshToken.hash("expired"))).isNull()
        assertThat(refreshTokenPersistenceAdapter.findByTokenHashForUpdate(RefreshToken.hash("active"))).isNotNull()
    }

    @Test
    fun `토큰으로 세션을 지운다`() {
        refreshTokenPersistenceAdapter.save(RefreshToken.issue(1L, "logout", now, lifetime))
        refreshTokenPersistenceAdapter.save(RefreshToken.issue(1L, "other", now, lifetime))
        flushAndClear()

        refreshTokenPersistenceAdapter.deleteByTokenHash(RefreshToken.hash("logout"))

        assertThat(refreshTokenPersistenceAdapter.findByTokenHashForUpdate(RefreshToken.hash("logout"))).isNull()
        assertThat(refreshTokenPersistenceAdapter.findByTokenHashForUpdate(RefreshToken.hash("other"))).isNotNull()
    }

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }

}
