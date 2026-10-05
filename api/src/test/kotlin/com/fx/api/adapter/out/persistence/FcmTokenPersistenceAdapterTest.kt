package com.fx.api.adapter.out.persistence

import com.fx.api.fixture.TopicFixture
import com.fx.common.adapter.out.persistence.repository.FcmTokenSubscriptionRepository
import com.fx.common.config.clock.ClockConfig
import com.fx.common.domain.DeviceType
import com.fx.common.domain.catalog.TopicView
import com.fx.common.domain.fcmtoken.FcmToken
import com.fx.common.domain.i18n.Language
import com.fx.common.exception.FcmTokenException
import com.fx.persistence.MySqlContainerConfig
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import

/** 실제 MySQL 에서 토큰 · 구독 저장을 검증한다. 스키마는 Flyway 로 만들고 엔티티 매핑과 맞는지 함께 검증한다. */
@DataJpaTest(properties = ["MYSQL_URL=unused", "MYSQL_DATABASE=unused", "MYSQL_USERNAME=unused", "MYSQL_PASSWORD=unused"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySqlContainerConfig::class, ClockConfig::class, FcmTokenPersistenceAdapter::class)
class FcmTokenPersistenceAdapterTest @Autowired constructor(
    private val fcmTokenPersistenceAdapter: FcmTokenPersistenceAdapter,
    private val fcmTokenSubscriptionRepository: FcmTokenSubscriptionRepository,
    private val entityManager: EntityManager,
) {

    @Test
    fun `토큰을 만들면 기본 토픽 구독도 함께 저장된다`() {
        val token = fcmTokenPersistenceAdapter.create(
            FcmToken("token-1", DeviceType.iOS),
            listOf(TopicFixture.GENERAL_NEWS, TopicFixture.STUDENT_CAFETERIA),
        )
        flushAndClear()

        val found = fcmTokenPersistenceAdapter.getByToken("token-1")
        assertThat(found.id).isEqualTo(token.id)
        assertThat(found.language).isEqualTo("ko")
        assertThat(found.createdAt).isNotNull()
        assertThat(fcmTokenPersistenceAdapter.findSubscribedTopicCodes(requireNotNull(token.id))).containsExactlyInAnyOrder(1, 900)
    }

    @Test
    fun `없는 토큰을 조회하면 예외가 발생한다`() {
        assertThat(fcmTokenPersistenceAdapter.findByToken("unknown")).isNull()
        assertThatThrownBy {
            fcmTokenPersistenceAdapter.getByToken("unknown")
        }.isInstanceOf(FcmTokenException::class.java)
    }

    @Test
    fun `대소문자만 다른 토큰은 서로 다른 토큰이다`() {
        val lowerId = createToken("token-abc")
        val upperId = createToken("token-ABC")

        assertThat(lowerId).isNotEqualTo(upperId)
        assertThat(fcmTokenPersistenceAdapter.getByToken("token-abc").id).isEqualTo(lowerId)
        assertThat(fcmTokenPersistenceAdapter.getByToken("token-ABC").id).isEqualTo(upperId)
    }

    @Test
    fun `알림 언어를 바꾸면 저장된다`() {
        createToken("token-1")

        fcmTokenPersistenceAdapter.getByToken("token-1").changeLanguage(Language.JA)
        flushAndClear()

        assertThat(fcmTokenPersistenceAdapter.getByToken("token-1").language).isEqualTo("ja")
    }

    @Test
    fun `같은 토픽을 두 번 구독해도 한 행만 남는다`() {
        val tokenId = createToken("token-1")

        fcmTokenPersistenceAdapter.subscribe(tokenId, TopicFixture.COMPUTER_SOFTWARE)
        fcmTokenPersistenceAdapter.subscribe(tokenId, TopicFixture.COMPUTER_SOFTWARE)
        flushAndClear()

        val subscriptions = fcmTokenSubscriptionRepository.findAllByFcmTokenId(tokenId)
        assertThat(subscriptions).hasSize(1)
        assertThat(subscriptions.single().topicCode).isEqualTo(300)
        assertThat(subscriptions.single().topicName).isEqualTo("COMPUTER_SOFTWARE")
    }

    @Test
    fun `구독을 해제하면 해당 토픽만 지워진다`() {
        val tokenId = createToken("token-1", TopicFixture.GENERAL_NEWS, TopicFixture.SCHOLARSHIP_NEWS)

        fcmTokenPersistenceAdapter.unsubscribe(tokenId, TopicFixture.GENERAL_NEWS.code)
        flushAndClear()

        assertThat(fcmTokenPersistenceAdapter.findSubscribedTopicCodes(tokenId)).containsExactly(2)
    }

    @Test
    fun `구독 복사는 대상 토큰의 구독을 원본과 같게 바꾼다`() {
        val fromId = createToken("old-token", TopicFixture.GENERAL_NEWS, TopicFixture.COMPUTER_SOFTWARE)
        val toId = createToken("new-token", TopicFixture.SCHOLARSHIP_NEWS)

        fcmTokenPersistenceAdapter.copySubscriptions(fromId, toId)
        flushAndClear()

        assertThat(fcmTokenPersistenceAdapter.findSubscribedTopicCodes(toId)).containsExactlyInAnyOrder(1, 300)
        assertThat(fcmTokenPersistenceAdapter.findSubscribedTopicCodes(fromId)).containsExactlyInAnyOrder(1, 300)
    }

    @Test
    fun `토큰 값을 바꾸면 같은 행이 갱신된다`() {
        val tokenId = createToken("old-token", TopicFixture.GENERAL_NEWS)

        fcmTokenPersistenceAdapter.getByToken("old-token").changeToken("new-token")
        flushAndClear()

        assertThat(fcmTokenPersistenceAdapter.findByToken("old-token")).isNull()
        assertThat(fcmTokenPersistenceAdapter.getByToken("new-token").id).isEqualTo(tokenId)
        assertThat(fcmTokenPersistenceAdapter.findSubscribedTopicCodes(tokenId)).containsExactly(1)
    }

    private fun createToken(token: String, vararg topics: TopicView): Long {
        val created = fcmTokenPersistenceAdapter.create(FcmToken(token, DeviceType.AOS), topics.toList())
        flushAndClear()
        return requireNotNull(created.id)
    }

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }

}
