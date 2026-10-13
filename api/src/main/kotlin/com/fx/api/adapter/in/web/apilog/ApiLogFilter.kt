package com.fx.api.adapter.`in`.web.apilog

import com.fx.api.application.port.`in`.apilog.dto.ApiLogCommand
import com.fx.common.domain.statistics.StatisticsTime
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerMapping
import java.time.Clock
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * 앱 Open API 요청을 한 건씩 [ApiLogBuffer] 에 남긴다.
 *
 * 디스패처 전체를 감싸는 필터라 최종 상태 코드와 직렬화까지 포함한 처리 시간을 보고, 처리되지 않은 예외가 빠져나가는 것도 잡는다.
 * 순서를 지정하지 않아 Spring Security 필터 뒤에서 실행된다.
 * 요청 스레드에서는 요청 속성을 읽어 큐에 넣기만 한다. 토큰 · 토픽 변환과 저장은 [ApiLogFlusher] 가 묶어서 한다.
 */
@Component
@ConditionalOnProperty(prefix = "api-log", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class ApiLogFilter(
    private val apiLogBuffer: ApiLogBuffer,
    private val clock: Clock,
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(ApiLogFilter::class.java)

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        !request.requestURI.startsWith(OPEN_API_PREFIX)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestedAt = StatisticsTime.now(clock)
        val startedAt = System.nanoTime()
        var failed = false
        try {
            filterChain.doFilter(request, response)
        } catch (e: Throwable) {
            // 빠져나간 예외는 서블릿 컨테이너가 500 으로 응답하지만, 이 시점의 response.status 는 아직 바뀌지 않았다
            failed = true
            throw e
        } finally {
            // 로그를 남기지 못해도 요청은 그대로 끝낸다
            runCatching {
                record(request, response, requestedAt, startedAt, failed)
            }.onFailure {
                log.warn("API 로그를 남기지 못했습니다", it)
            }
        }
    }

    /** 컨트롤러 메서드가 처리한 요청만 남긴다. 정적 리소스 · 매핑되지 않은 경로는 URL 패턴이 없다. */
    private fun record(
        request: HttpServletRequest,
        response: HttpServletResponse,
        requestedAt: LocalDateTime,
        startedAt: Long,
        failed: Boolean,
    ) {
        if (request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE) !is HandlerMethod) {
            return
        }
        val urlPattern =
            request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)?.toString()
                ?: return

        apiLogBuffer.offer(
            ApiLogCommand(
                requestedAt = requestedAt,
                method = request.method,
                urlPattern = urlPattern,
                statusCode = if (failed) HttpStatus.INTERNAL_SERVER_ERROR.value() else response.status,
                durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt)
                    .coerceAtMost(Int.MAX_VALUE.toLong())
                    .toInt(),
                topicName = if (urlPattern == NOTICES_V1_PATTERN) request.getParameter("topic") else null,
                topicId = if (urlPattern == NOTICES_V2_PATTERN) request.getParameter("topicId") else null,
                fcmToken = request.getHeader("fcmToken")?.takeIf {
                    it.isNotBlank()
                },
            )
        )
    }

    companion object {

        private const val OPEN_API_PREFIX = "/open-api/"

        // 토픽별 통계는 공지 목록 조회만 센다
        private const val NOTICES_V1_PATTERN = "/open-api/v1/notices"
        private const val NOTICES_V2_PATTERN = "/open-api/v2/notices"

    }

}
