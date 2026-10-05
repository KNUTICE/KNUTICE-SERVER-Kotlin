package com.fx.common.concurrent

import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * 작업을 가상 스레드에서 병렬로 실행하되 동시에 [maxConcurrency] 개까지만 돌린다.
 *
 * 가상 스레드는 스레드 수 제한이 없으므로 원격 사이트 · 외부 API 처럼 자원 한도가 있는 대상마다
 * 인스턴스를 따로 만들어 상한을 건다. 한 인스턴스의 상한은 동시에 호출된 [invokeAll] 전체에 걸린다.
 */
class BoundedParallelExecutor(
    private val maxConcurrency: Int,
) {

    private val permits = Semaphore(maxConcurrency)

    init {
        require(maxConcurrency > 0) {
            "동시 실행 수는 1 이상이어야 합니다."
        }
    }

    /**
     * 모든 작업을 실행하고 결과를 작업 순서대로 돌려준다. 실패한 작업은 예외를 담은 [Result] 로 돌려준다.
     * [timeout] 안에 끝나지 않은 작업은 취소(인터럽트)되고 [TimeoutException] 결과가 된다.
     */
    fun <T> invokeAll(tasks: List<() -> T>, timeout: Duration): List<Result<T>> {
        if (tasks.isEmpty()) {
            return emptyList()
        }

        Executors.newVirtualThreadPerTaskExecutor().use { executor ->
            val callables = tasks.map { task ->
                Callable {
                    permits.acquire()
                    try {
                        task()
                    } finally {
                        permits.release()
                    }
                }
            }
            return executor.invokeAll(callables, timeout.toMillis(), TimeUnit.MILLISECONDS).map { future ->
                try {
                    Result.success(future.get())
                } catch (e: CancellationException) {
                    Result.failure(TimeoutException("작업이 ${timeout.toMillis()}ms 안에 끝나지 않아 취소했습니다."))
                } catch (e: ExecutionException) {
                    Result.failure(e.cause ?: e)
                }
            }
        }
    }

}
