package com.fx.api.adapter.`in`.web.apilog

import com.fx.api.application.port.`in`.apilog.dto.ApiLogCommand
import com.fx.api.config.apilog.ApiLogProperties
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class ApiLogFilterTest : BehaviorSpec({

    // JVM 시간대와 상관없이 KST 로 남는지 보려고 UTC 시계를 쓴다
    val clock = Clock.fixed(Instant.parse("2026-10-13T14:59:59.500Z"), ZoneOffset.UTC)
    val handlerMethod = HandlerMethod(Any(), Any::class.java.getMethod("toString"))

    fun request(method: String, uri: String) =
        MockHttpServletRequest(method, uri)

    /** 디스패처처럼 매핑한 핸들러 · URL 패턴을 요청 속성에 남기고 응답한다. */
    fun dispatch(
        urlPattern: String?,
        handler: Any? = handlerMethod,
        status: Int = 200,
        action: () -> Unit = {},
    ) =
        FilterChain { request, response ->
            handler?.let {
                request.setAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE, it)
            }
            urlPattern?.let {
                request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, it)
            }
            (response as HttpServletResponse).status = status
            action()
        }

    fun filterWithBuffer(): Pair<ApiLogFilter, ApiLogBuffer> {
        val buffer = ApiLogBuffer(ApiLogProperties(), SimpleMeterRegistry())
        return ApiLogFilter(buffer, clock) to buffer
    }

    fun ApiLogFilter.handle(request: HttpServletRequest, chain: FilterChain) {
        doFilter(request, MockHttpServletResponse(), chain)
    }

    Given("컨트롤러가 처리한 Open API 요청") {

        When("정상 응답하면") {
            val (filter, buffer) = filterWithBuffer()
            val request = request("GET", "/open-api/v1/topics").apply {
                addHeader("fcmToken", "token-1")
            }

            filter.handle(
                request,
                dispatch("/open-api/v1/topics", status = 201) {
                    Thread.sleep(20)
                },
            )

            Then("KST 요청 시각 · 메서드 · URL 패턴 · 상태 · 처리 시간 · 토큰을 남긴다") {
                val logged = buffer.drain(10).single()
                logged.requestedAt shouldBe LocalDateTime.of(2026, 10, 13, 23, 59, 59, 500_000_000)
                logged.method shouldBe "GET"
                logged.urlPattern shouldBe "/open-api/v1/topics"
                logged.statusCode shouldBe 201
                logged.durationMs shouldBeGreaterThanOrEqual 20
                logged.fcmToken shouldBe "token-1"
                logged.topicName shouldBe null
                logged.topicId shouldBe null
            }
        }

        When("실제 경로 대신") {
            val (filter, buffer) = filterWithBuffer()

            filter.handle(request("GET", "/open-api/v1/notices/1234567"), dispatch("/open-api/v1/notices/{nttId}"))

            Then("URL 패턴으로 남긴다") {
                buffer.drain(10).single().urlPattern shouldBe "/open-api/v1/notices/{nttId}"
            }
        }

        When("토큰 헤더가 비어 있으면") {
            val (filter, buffer) = filterWithBuffer()
            val request = request("GET", "/open-api/v1/topics").apply {
                addHeader("fcmToken", " ")
            }

            filter.handle(request, dispatch("/open-api/v1/topics"))

            Then("토큰을 남기지 않는다") {
                buffer.drain(10).single().fcmToken shouldBe null
            }
        }
    }

    Given("공지 목록 조회") {

        When("v1 이면") {
            val (filter, buffer) = filterWithBuffer()
            val request = request("GET", "/open-api/v1/notices").apply {
                setParameter("topic", "GENERAL_NEWS")
                setParameter("topicId", "1")
            }

            filter.handle(request, dispatch("/open-api/v1/notices"))

            Then("topic 파라미터(토픽 이름)만 남긴다") {
                val logged = buffer.drain(10).single()
                logged.topicName shouldBe "GENERAL_NEWS"
                logged.topicId shouldBe null
            }
        }

        When("v2 면") {
            val (filter, buffer) = filterWithBuffer()
            val request = request("GET", "/open-api/v2/notices").apply {
                setParameter("topic", "GENERAL_NEWS")
                setParameter("topicId", "1")
            }

            filter.handle(request, dispatch("/open-api/v2/notices"))

            Then("topicId 파라미터(토픽 코드)만 남긴다") {
                val logged = buffer.drain(10).single()
                logged.topicName shouldBe null
                logged.topicId shouldBe "1"
            }
        }

        When("공지 목록이 아닌 요청에 토픽 파라미터가 있으면") {
            val (filter, buffer) = filterWithBuffer()
            val request = request("GET", "/open-api/v1/notices/1").apply {
                setParameter("topic", "GENERAL_NEWS")
                setParameter("topicId", "1")
            }

            filter.handle(request, dispatch("/open-api/v1/notices/{nttId}"))

            Then("토픽을 남기지 않는다") {
                val logged = buffer.drain(10).single()
                logged.topicName shouldBe null
                logged.topicId shouldBe null
            }
        }
    }

    Given("남기지 않는 요청") {

        When("Open API 가 아니면") {
            val (filter, buffer) = filterWithBuffer()

            filter.handle(request("GET", "/api/v1/notices"), dispatch("/api/v1/notices"))
            filter.handle(request("GET", "/actuator/prometheus"), dispatch("/actuator/prometheus"))

            Then("남기지 않는다") {
                buffer.drain(10).shouldBeEmpty()
            }
        }

        When("컨트롤러 메서드가 처리하지 않았으면") {
            val (filter, buffer) = filterWithBuffer()

            filter.handle(request("GET", "/open-api/images/a.png"), dispatch("/open-api/images/**", handler = ResourceHttpRequestHandler()))
            filter.handle(request("GET", "/open-api/unknown"), dispatch(null, handler = null, status = 404))

            Then("남기지 않는다") {
                buffer.drain(10).shouldBeEmpty()
            }
        }
    }

    Given("처리되지 않은 예외가 빠져나가는 요청") {
        val (filter, buffer) = filterWithBuffer()
        val error = IllegalStateException("처리되지 않은 오류")

        When("필터를 지나면") {
            val thrown = shouldThrow<IllegalStateException> {
                filter.handle(
                    request("POST", "/open-api/v1/reports"),
                    dispatch("/open-api/v1/reports") {
                        throw error
                    },
                )
            }

            Then("예외는 그대로 던지고 상태 코드 500 으로 남긴다") {
                thrown shouldBeSameInstanceAs error
                val logged = buffer.drain(10).single()
                logged.method shouldBe "POST"
                logged.statusCode shouldBe 500
            }
        }
    }

    Given("로그를 남기다 오류가 나면") {
        val buffer = mockk<ApiLogBuffer>()
        every {
            buffer.offer(any<ApiLogCommand>())
        } throws IllegalStateException("버퍼 오류")
        val filter = ApiLogFilter(buffer, clock)
        val response = MockHttpServletResponse()
        var handled = false

        When("요청을 처리하면") {
            filter.doFilter(
                request("GET", "/open-api/v1/topics"),
                response,
                dispatch("/open-api/v1/topics") {
                    handled = true
                },
            )

            Then("요청은 그대로 끝난다") {
                handled shouldBe true
                response.status shouldBe 200
            }
        }
    }

})
