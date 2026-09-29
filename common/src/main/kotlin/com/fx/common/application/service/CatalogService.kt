package com.fx.common.application.service

import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.application.port.out.CatalogPersistencePort
import com.fx.common.domain.catalog.NotificationTemplateCatalog
import com.fx.common.domain.catalog.TopicCatalog
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * 토픽 · 단과대 · 알림 문구 스냅샷을 메모리에 캐시한다.
 *
 * api 와 crawler 는 별도 프로세스라 한쪽의 변경을 다른 쪽이 알 수 없다.
 * 그래서 [TTL] 이 지나면 다시 읽고, 변경을 아는 쪽은 [refresh] 로 즉시 버린다.
 *
 * 캐시가 살아 있는 동안은 DB 에 가지 않도록 트랜잭션을 걸지 않는다.
 * 여러 테이블을 한 번에 읽는 일관성은 [CatalogPersistencePort] 구현이 맡는다.
 */
@Service
class CatalogService(
    private val catalogPersistencePort: CatalogPersistencePort,
    private val clock: Clock,
) : CatalogQueryUseCase {

    private val topicCatalogCache = SnapshotCache(clock) { catalogPersistencePort.loadTopicCatalog() }
    private val notificationTemplateCatalogCache = SnapshotCache(clock) { catalogPersistencePort.loadNotificationTemplateCatalog() }

    override fun getTopicCatalog(): TopicCatalog = topicCatalogCache.get()

    override fun getNotificationTemplateCatalog(): NotificationTemplateCatalog = notificationTemplateCatalogCache.get()

    override fun refresh() {
        topicCatalogCache.evict()
        notificationTemplateCatalogCache.evict()
    }

    companion object {
        val TTL: Duration = Duration.ofMinutes(5)
    }

    private class SnapshotCache<T : Any>(
        private val clock: Clock,
        private val loader: () -> T,
    ) {

        private class Entry<T>(val value: T, val loadedAt: Instant)

        @Volatile
        private var entry: Entry<T>? = null

        fun get(): T {
            fresh()?.let { return it }
            synchronized(this) {
                fresh()?.let { return it }
                return loader().also { entry = Entry(it, clock.instant()) }
            }
        }

        fun evict() {
            entry = null
        }

        private fun fresh(): T? =
            entry?.takeIf { clock.instant().isBefore(it.loadedAt.plus(TTL)) }?.value

    }

}
