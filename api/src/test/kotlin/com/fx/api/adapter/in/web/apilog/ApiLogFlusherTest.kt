package com.fx.api.adapter.`in`.web.apilog

import com.fx.api.application.port.`in`.apilog.ApiLogCommandUseCase
import com.fx.api.application.port.`in`.apilog.dto.ApiLogCommand
import com.fx.api.config.apilog.ApiLogProperties
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.boot.web.server.context.WebServerApplicationContext
import java.time.LocalDateTime

class ApiLogFlusherTest : BehaviorSpec({

    val properties = ApiLogProperties(batchSize = 500)

    fun command(durationMs: Int) =
        ApiLogCommand(
            requestedAt = LocalDateTime.of(2026, 10, 13, 12, 0),
            method = "GET",
            urlPattern = "/open-api/v1/topics",
            statusCode = 200,
            durationMs = durationMs,
        )

    /** 저장한 묶음의 크기를 순서대로 남긴다. [failAt] 번째(1부터) 묶음은 저장에 실패한다. */
    class Fixture(count: Int, failAt: Int? = null) {
        val meterRegistry = SimpleMeterRegistry()
        val buffer = ApiLogBuffer(properties, meterRegistry)
        val savedSizes = mutableListOf<Int>()
        val useCase = mockk<ApiLogCommandUseCase>()
        val flusher = ApiLogFlusher(buffer, useCase, properties, meterRegistry)

        init {
            var calls = 0
            every {
                useCase.saveAll(any())
            } answers {
                calls++
                if (calls == failAt) {
                    throw IllegalStateException("DB 오류")
                }
                savedSizes += firstArg<List<ApiLogCommand>>().size
            }
            (1..count).forEach {
                buffer.offer(command(it))
            }
        }

        fun saveFailedCount(): Double =
            meterRegistry.get("api.log.dropped").tag("reason", "save_failed").counter().count()
    }

    Given("묶음 크기보다 많이 쌓이면") {
        val fixture = Fixture(count = 1_200)

        When("저장하면") {
            fixture.flusher.flush()

            Then("버퍼가 빌 때까지 묶음 크기씩 반복해 저장한다") {
                fixture.savedSizes shouldBe listOf(500, 500, 200)
                fixture.buffer.drain(10).shouldBeEmpty()
            }
        }
    }

    Given("묶음 크기의 배수만큼 쌓이면") {
        val fixture = Fixture(count = 1_000)

        When("저장하면") {
            fixture.flusher.flush()

            Then("빈 묶음은 저장하지 않는다") {
                fixture.savedSizes shouldBe listOf(500, 500)
            }
        }
    }

    Given("버퍼가 비어 있으면") {
        val fixture = Fixture(count = 0)

        When("저장하면") {
            fixture.flusher.flush()

            Then("저장을 부르지 않는다") {
                verify(exactly = 0) {
                    fixture.useCase.saveAll(any())
                }
            }
        }
    }

    Given("첫 묶음의 저장이 실패하면") {
        val fixture = Fixture(count = 700, failAt = 1)

        When("저장하면") {
            fixture.flusher.flush()

            Then("그 묶음은 버리고 save_failed 로 센 뒤 다음 묶음을 저장한다") {
                fixture.saveFailedCount() shouldBe 500.0
                fixture.savedSizes shouldBe listOf(200)
                fixture.buffer.drain(10).shouldBeEmpty()
            }
        }
    }

    Given("종료할 때 로그가 남아 있으면") {
        val fixture = Fixture(count = 3)
        fixture.flusher.start()

        When("멈추면") {
            fixture.flusher.isRunning shouldBe true
            fixture.flusher.stop()

            Then("남은 로그를 모두 저장한다") {
                fixture.savedSizes shouldBe listOf(3)
                fixture.flusher.isRunning shouldBe false
            }
        }
    }

    Given("lifecycle 순서") {
        val fixture = Fixture(count = 0)

        Then("웹 서버가 멈춘 뒤에 멈춘다") {
            fixture.flusher.phase shouldBeLessThan WebServerApplicationContext.START_STOP_LIFECYCLE_PHASE
        }
    }

})
