package com.fx.api.adapter.`in`.web.apilog

import com.fx.api.application.port.`in`.apilog.dto.ApiLogCommand
import com.fx.api.config.apilog.ApiLogProperties
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.time.LocalDateTime

class ApiLogBufferTest : BehaviorSpec({

    fun command(durationMs: Int) =
        ApiLogCommand(
            requestedAt = LocalDateTime.of(2026, 10, 13, 12, 0),
            method = "GET",
            urlPattern = "/open-api/v1/topics",
            statusCode = 200,
            durationMs = durationMs,
        )

    Given("용량이 2인 버퍼") {

        When("용량보다 많이 넣으면") {
            val meterRegistry = SimpleMeterRegistry()
            val buffer = ApiLogBuffer(ApiLogProperties(bufferCapacity = 2), meterRegistry)

            (1..3).forEach {
                buffer.offer(command(it))
            }

            Then("넘친 로그는 버리고 buffer_full 로 센다") {
                meterRegistry.get("api.log.buffer.size").gauge().value() shouldBe 2.0
                meterRegistry.get("api.log.dropped").tag("reason", "buffer_full").counter().count() shouldBe 1.0
                buffer.drain(10).map {
                    it.durationMs
                } shouldBe listOf(1, 2)
                meterRegistry.get("api.log.buffer.size").gauge().value() shouldBe 0.0
            }
        }
    }

    Given("로그가 5건 쌓인 버퍼") {
        val buffer = ApiLogBuffer(ApiLogProperties(), SimpleMeterRegistry())
        (1..5).forEach {
            buffer.offer(command(it))
        }

        When("최대 건수를 정해 꺼내면") {
            Then("넣은 순서대로 최대 건수까지만 꺼낸다") {
                buffer.drain(2).map {
                    it.durationMs
                } shouldBe listOf(1, 2)
                buffer.drain(10) shouldHaveSize 3
                buffer.drain(10).shouldBeEmpty()
            }
        }
    }

})
