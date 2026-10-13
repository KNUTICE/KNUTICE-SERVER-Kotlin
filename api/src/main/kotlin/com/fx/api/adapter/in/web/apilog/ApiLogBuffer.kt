package com.fx.api.adapter.`in`.web.apilog

import com.fx.api.application.port.`in`.apilog.dto.ApiLogCommand
import com.fx.api.config.apilog.ApiLogProperties
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.concurrent.ArrayBlockingQueue

/**
 * 요청 스레드가 남긴 로그를 저장 전까지 담아 두는 메모리 큐.
 *
 * 크기에 상한이 있어 DB 장애가 길어져도 메모리가 계속 늘지 않는다.
 * 가득 차면 요청을 기다리게 하지 않고 로그를 버린다.
 */
@Component
@ConditionalOnProperty(prefix = "api-log", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class ApiLogBuffer(
    properties: ApiLogProperties,
    meterRegistry: MeterRegistry,
) {

    private val queue = ArrayBlockingQueue<ApiLogCommand>(properties.bufferCapacity)

    private val bufferFullCounter = droppedCounter(meterRegistry, "buffer_full")

    init {
        Gauge.builder("api.log.buffer.size", queue) {
            it.size.toDouble()
        }
            .description("저장을 기다리는 API 로그 수")
            .register(meterRegistry)
    }

    /** 기다리지 않는다. 가득 차 있으면 버린다. */
    fun offer(command: ApiLogCommand) {
        if (!queue.offer(command)) {
            bufferFullCounter.increment()
        }
    }

    /** 최대 [max] 건을 꺼낸다. */
    fun drain(max: Int): List<ApiLogCommand> =
        ArrayList<ApiLogCommand>(max).also {
            queue.drainTo(it, max)
        }

    companion object {

        /** 버린 로그 수. 버퍼와 저장 스케줄러가 사유만 달리해 같은 지표에 센다. */
        fun droppedCounter(meterRegistry: MeterRegistry, reason: String): Counter =
            Counter.builder("api.log.dropped")
                .tag("reason", reason)
                .description("저장하지 못하고 버린 API 로그 수")
                .register(meterRegistry)

    }

}
