package com.fx.api.adapter.`in`.web.apilog

import com.fx.api.application.port.`in`.apilog.ApiLogCommandUseCase
import com.fx.api.application.port.`in`.apilog.dto.ApiLogCommand
import com.fx.api.config.apilog.ApiLogProperties
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.server.context.WebServerApplicationContext
import org.springframework.context.SmartLifecycle
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * [ApiLogBuffer] 에 쌓인 로그를 주기적으로 묶어 저장한다.
 *
 * 저장에 실패한 묶음은 버리고 다시 넣지 않는다. 장애 중에 다시 넣으면 버퍼만 더 빨리 찬다.
 * 종료할 때는 웹 서버가 처리 중인 요청을 끝내고 멈춘 뒤에 남은 로그를 모두 저장한다 (DataSource 는 그 다음에 닫힌다).
 */
@Component
@ConditionalOnProperty(prefix = "api-log", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class ApiLogFlusher(
    private val apiLogBuffer: ApiLogBuffer,
    private val apiLogCommandUseCase: ApiLogCommandUseCase,
    private val properties: ApiLogProperties,
    meterRegistry: MeterRegistry,
) : SmartLifecycle {

    private val log = LoggerFactory.getLogger(ApiLogFlusher::class.java)

    private val saveFailedCounter =
        ApiLogBuffer.droppedCounter(meterRegistry, "save_failed")

    @Volatile
    private var running = false

    /** 벽시계 시각에 맞출 필요가 없는 주기 작업이라 `fixedDelay` 로 돈다. 꺼낸 묶음이 가득 차 있으면 버퍼가 빌 때까지 반복한다. */
    @Scheduled(fixedDelayString = "\${api-log.flush-interval:1s}")
    fun flush() {
        do {
            val commands = apiLogBuffer.drain(properties.batchSize)
            if (commands.isNotEmpty()) {
                save(commands)
            }
        } while (commands.size >= properties.batchSize)
    }

    private fun save(commands: List<ApiLogCommand>) {
        runCatching {
            apiLogCommandUseCase.saveAll(commands)
        }.onFailure {
            saveFailedCounter.increment(commands.size.toDouble())
            log.error("API 로그 {}건을 저장하지 못해 버렸습니다", commands.size, it)
        }
    }

    override fun start() {
        running = true
    }

    override fun stop() {
        running = false
        flush()
    }

    override fun isRunning(): Boolean =
        running

    /** 웹 서버보다 늦게 멈춘다 (phase 가 낮을수록 나중에 멈춘다). */
    override fun getPhase(): Int =
        WebServerApplicationContext.START_STOP_LIFECYCLE_PHASE - 1

}
