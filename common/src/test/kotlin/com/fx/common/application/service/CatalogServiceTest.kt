package com.fx.common.application.service

import com.fx.common.application.port.out.CatalogPersistencePort
import com.fx.common.domain.catalog.NotificationTemplateCatalog
import com.fx.common.domain.catalog.TopicCatalog
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class CatalogServiceTest {

    private class MutableClock(var now: Instant) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId?): Clock = this
        override fun instant(): Instant = now
    }

    private class CountingPort : CatalogPersistencePort {
        var topicLoads = 0
        var templateLoads = 0
        override fun loadTopicCatalog(): TopicCatalog = TopicCatalog(emptyList(), emptyList()).also { topicLoads++ }
        override fun loadNotificationTemplateCatalog(): NotificationTemplateCatalog = NotificationTemplateCatalog(emptyMap()).also { templateLoads++ }
    }

    private val clock = MutableClock(Instant.parse("2026-09-29T00:00:00Z"))
    private val port = CountingPort()
    private val service = CatalogService(port, clock)

    @Test
    fun `TTL 안에서는 DB 를 다시 읽지 않는다`() {
        val first = service.getTopicCatalog()
        clock.now = clock.now.plus(CatalogService.TTL).minusSeconds(1)
        val second = service.getTopicCatalog()

        assertThat(second).isSameAs(first)
        assertThat(port.topicLoads).isEqualTo(1)
    }

    @Test
    fun `TTL 이 지나면 다시 읽는다`() {
        service.getTopicCatalog()
        clock.now = clock.now.plus(CatalogService.TTL)
        service.getTopicCatalog()

        assertThat(port.topicLoads).isEqualTo(2)
    }

    @Test
    fun `refresh 하면 다음 조회 때 토픽 · 알림 문구를 다시 읽는다`() {
        service.getTopicCatalog()
        service.getNotificationTemplateCatalog()

        service.refresh()
        service.getTopicCatalog()
        service.getNotificationTemplateCatalog()

        assertThat(port.topicLoads).isEqualTo(2)
        assertThat(port.templateLoads).isEqualTo(2)
        assertThat(Duration.ofMinutes(5)).isEqualTo(CatalogService.TTL)
    }

}
