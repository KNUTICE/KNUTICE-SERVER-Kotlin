package com.fx.common.concurrent

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger

class BoundedParallelExecutorTest {

    @Test
    fun `결과를 작업 순서대로 돌려주고 실패는 결과에 담는다`() {
        val executor = BoundedParallelExecutor(maxConcurrency = 4)

        val results = executor.invokeAll(
            listOf({ "a" }, { error("실패") }, { "c" }),
            Duration.ofSeconds(5),
        )

        assertThat(results.map { it.getOrNull() }).containsExactly("a", null, "c")
        assertThat(results[1].exceptionOrNull()).isInstanceOf(IllegalStateException::class.java).hasMessage("실패")
    }

    @Test
    fun `동시에 실행되는 작업 수가 상한을 넘지 않는다`() {
        val executor = BoundedParallelExecutor(maxConcurrency = 3)
        val running = AtomicInteger()
        val maxRunning = AtomicInteger()

        val results = executor.invokeAll(
            List(20) {
                {
                    val current = running.incrementAndGet()
                    maxRunning.accumulateAndGet(current, ::maxOf)
                    Thread.sleep(20)
                    running.decrementAndGet()
                }
            },
            Duration.ofSeconds(10),
        )

        assertThat(results).allMatch { it.isSuccess }
        assertThat(maxRunning.get()).isEqualTo(3)
    }

    @Test
    fun `제한 시간 안에 끝나지 않은 작업은 취소하고 타임아웃 결과를 돌려준다`() {
        val executor = BoundedParallelExecutor(maxConcurrency = 2)

        val results = executor.invokeAll(
            listOf({ "빠름" }, { Thread.sleep(5_000); "느림" }),
            Duration.ofMillis(300),
        )

        assertThat(results[0].getOrNull()).isEqualTo("빠름")
        assertThat(results[1].exceptionOrNull()).isInstanceOf(TimeoutException::class.java)
    }

    @Test
    fun `동시 실행 수는 1 이상이어야 한다`() {
        assertThatThrownBy { BoundedParallelExecutor(0) }.isInstanceOf(IllegalArgumentException::class.java)
    }

}
