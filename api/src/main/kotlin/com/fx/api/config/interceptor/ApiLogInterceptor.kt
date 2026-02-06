package com.fx.api.config.interceptor

import com.fx.api.application.port.`in`.ApiLogCommandUseCase
import com.fx.api.application.port.`in`.dto.ApiLogSaveCommand
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.HandlerMapping
import java.lang.Exception

/**
 * API 요청 및 응답 로그 수집 인터셉터
 *
 * @author 이동섭
 * @since 2026-02-06
 */
@Component
class ApiLogInterceptor(
    private val apiLogCommandUseCase: ApiLogCommandUseCase
) : HandlerInterceptor {

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        // 시작 시간 기록
        request.setAttribute("startTime", System.currentTimeMillis())
        return true;
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        if (handler !is HandlerMethod) return

        val startTime = request.getAttribute("startTime") as Long
        val executionTime = System.currentTimeMillis() - startTime

        val urlPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE).toString();
        val queryParameter = request.parameterMap.mapValues { it.value.joinToString(",") }
            .takeIf { it.isNotEmpty() }
        val fcmToken = request.getHeader("fcmToken")

        val clientIp = getClientIp(request)

        val apiLogSaveCommand = ApiLogSaveCommand(
            urlPattern = urlPattern,
            url = request.requestURI,
            method = request.method,
            queryParameters = queryParameter,
            fcmToken = fcmToken,
            clientIp = clientIp,
            statusCode = response.status,
            executionTime = executionTime
        )

        apiLogCommandUseCase.recordApiLog(apiLogSaveCommand)
    }

    private fun getClientIp(request: HttpServletRequest): String =
        request.getHeader("X-Forwarded-For")?.substringBefore(",") ?: request.remoteAddr

}