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
 * 행 수가 적고 드물게 바뀌는 데이터라, 요청마다 DB 를 조회하거나 JOIN 하지 않고
 * 전체를 한 번 읽어 [TTL] 동안 재사용한다. 기동할 때 미리 읽지 않고 처음 조회할 때 읽는다.
 *
 * api 와 crawler 는 별도 프로세스라 캐시도 각자 가지며, 한쪽의 변경을 다른 쪽이 알 수 없다.
 * 그래서 [TTL] 이 지나면 다시 읽고, 변경을 아는 쪽은 [refresh] 로 즉시 버린다.
 *
 * 캐시가 살아 있는 동안은 DB 커넥션을 잡지 않도록 이 서비스에는 트랜잭션을 걸지 않는다.
 * 여러 테이블(토픽 · 단과대)을 한 시점 기준으로 읽는 일관성은 [CatalogPersistencePort] 구현이 맡는다.
 */
@Service
class CatalogService(
    private val catalogPersistencePort: CatalogPersistencePort,
    private val clock: Clock,
) : CatalogQueryUseCase {

    /** 토픽 · 단과대 카탈로그 캐시. 알림 문구 캐시와 잠금을 따로 가져 서로의 재적재를 기다리지 않는다. */
    private val topicCatalogCache =
        SnapshotCache(clock) {
            catalogPersistencePort.loadTopicCatalog()
        }

    /** 알림 문구 카탈로그 캐시. */
    private val notificationTemplateCatalogCache =
        SnapshotCache(clock) {
            catalogPersistencePort.loadNotificationTemplateCatalog()
        }

    /** 캐시가 [TTL] 안이면 메모리 값을, 비었거나 만료됐으면 DB 에서 새로 읽은 값을 돌려준다. */
    override fun getTopicCatalog(): TopicCatalog =
        topicCatalogCache.get()

    /** 캐시가 [TTL] 안이면 메모리 값을, 비었거나 만료됐으면 DB 에서 새로 읽은 값을 돌려준다. */
    override fun getNotificationTemplateCatalog(): NotificationTemplateCatalog =
        notificationTemplateCatalogCache.get()

    /**
     * 두 캐시를 모두 비운다. 바로 다시 읽지 않고 다음 조회 때 읽는다.
     * 트랜잭션 안에서 부르면 커밋 전 상태를 다시 캐시할 수 있으므로 커밋이 끝난 뒤에 부른다.
     */
    override fun refresh() {
        topicCatalogCache.evict()
        notificationTemplateCatalogCache.evict()
    }

    companion object {
        /** 스냅샷 유효 시간. 다른 프로세스의 변경이 늦어도 이 시간 안에 반영된다. */
        val TTL: Duration = Duration.ofMinutes(5)
    }

    /**
     * 값 하나를 [TTL] 동안 들고 있는 캐시. 비었거나 만료되면 [loader] 로 다시 읽는다.
     *
     * 대부분의 조회는 잠금 없이 끝나고(`@Volatile` 로 다른 스레드가 저장한 값을 바로 본다),
     * 다시 읽어야 할 때만 잠금을 걸어 여러 스레드가 동시에 DB 를 읽지 않게 한다 (이중 확인 잠금).
     *
     * @param loader DB 에서 스냅샷을 새로 만드는 함수
     */
    private class SnapshotCache<T : Any>(
        private val clock: Clock,
        private val loader: () -> T,
    ) {

        /** 캐시한 값과 읽기를 마친 시각. 불변이라 여러 스레드가 함께 읽어도 안전하다. */
        private class Entry<T>(val value: T, val loadedAt: Instant)

        /** 비어 있으면 null. 교체는 참조 하나를 바꾸는 것이라 잠금 없이도 원자적이다. */
        @Volatile
        private var entry: Entry<T>? = null

        /**
         * 1. 잠금 없이 확인해 유효하면 바로 돌려준다.
         * 2. 비었거나 만료됐으면 잠금을 건다. 잠금을 기다리는 동안 다른 스레드가 이미 채웠을 수 있으므로 한 번 더 확인한다.
         * 3. 그래도 없으면 DB 에서 읽어 저장한다. 만료 순간 요청이 몰려도 DB 는 한 번만 읽는다.
         */
        fun get(): T {
            fresh()?.let {
                return it
            }
            synchronized(this) {
                fresh()?.let {
                    return it
                }
                return loader().also {
                    // TTL 은 읽기를 마친 시각부터 센다
                    entry = Entry(it, clock.instant())
                }
            }
        }

        /**
         * 값을 버린다. 잠금 없이 비우므로, 이미 DB 를 읽고 있던 스레드가 끝나며 그 결과를 저장할 수 있다.
         * 그 경우에도 [TTL] 이 지나면 다시 읽는다.
         */
        fun evict() {
            entry = null
        }

        /** [TTL] 안의 값이면 돌려주고, 비었거나 만료됐으면 null. */
        private fun fresh(): T? =
            entry?.takeIf {
                clock.instant().isBefore(it.loadedAt.plus(TTL))
            }?.value

    }

}
