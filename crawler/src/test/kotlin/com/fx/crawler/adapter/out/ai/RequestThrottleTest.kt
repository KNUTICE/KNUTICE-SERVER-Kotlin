package com.fx.crawler.adapter.out.ai

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class RequestThrottleTest {

    private val start = Instant.parse("2026-10-01T03:00:00Z")

    @Test
    fun `앞 요청과의 간격이 모자라면 남은 시간만큼 기다린다`() {
        val clock = MutableClock(start)
        val waits = mutableListOf<Duration>()
        val throttle = RequestThrottle(Duration.ofSeconds(5), clock) {
            waits += it
            clock.now = clock.now.plus(it)
        }

        throttle.acquire()
        clock.now = clock.now.plusSeconds(2)
        throttle.acquire()
        clock.now = clock.now.plusSeconds(10)
        throttle.acquire()

        // 첫 요청은 바로, 두 번째는 5초 간격을 채우려고 3초, 세 번째는 이미 간격이 지나 바로 보낸다
        assertThat(waits).containsExactly(Duration.ofSeconds(3))
    }

    @Test
    fun `동시에 불러도 요청 시각이 간격만큼 벌어진다`() {
        val waits = mutableListOf<Duration>()
        val throttle = RequestThrottle(Duration.ofSeconds(5), Clock.fixed(start, ZoneOffset.UTC)) {
            waits += it
        }

        repeat(3) {
            throttle.acquire()
        }

        assertThat(waits).containsExactly(Duration.ofSeconds(5), Duration.ofSeconds(10))
    }

    private class MutableClock(var now: Instant) : Clock() {
        override fun instant(): Instant =
            now
        override fun getZone(): ZoneId =
            ZoneOffset.UTC
        override fun withZone(zone: ZoneId?): Clock =
            this
    }

}
