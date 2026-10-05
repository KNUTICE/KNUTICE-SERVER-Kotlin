package com.fx.crawler.adapter.out.ai

import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * 요청 사이에 [interval] 이상 간격을 둔다.
 * 부를 때마다 다음 요청 시각을 미리 정해 두므로 여러 스레드가 동시에 불러도 요청 시각이 겹치지 않는다.
 */
class RequestThrottle(
    private val interval: Duration,
    private val clock: Clock,
    private val sleeper: (Duration) -> Unit = {
        Thread.sleep(it)
    },
) {

    private var nextAllowedAt: Instant = Instant.MIN

    /** 앞 요청과의 간격이 [interval] 이 될 때까지 기다린다. */
    fun acquire() {
        val wait = synchronized(this) {
            val now = clock.instant()
            val startAt = maxOf(now, nextAllowedAt)
            nextAllowedAt = startAt.plus(interval)
            Duration.between(now, startAt)
        }
        if (!wait.isZero) {
            sleeper(wait)
        }
    }

}
