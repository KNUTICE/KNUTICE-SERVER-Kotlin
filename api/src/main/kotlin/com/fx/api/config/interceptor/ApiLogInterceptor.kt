package com.fx.api.config.interceptor

import com.fx.api.domain.ApiLog
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
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
//    private val ApiLogPersistenceAdapter: ApiLogPersistenceAdapter
) : HandlerInterceptor {

    private val log = LoggerFactory.getLogger(ApiLogInterceptor::class.java)

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
        val fcmToken = request.getHeader("fcmToken")

        val clientIp = getClientIp(request)

        val apiLog = ApiLog(
            urlPattern = urlPattern,
            url = request.requestURI,
            method = request.method,
            queryParameters = queryParameter.toMutableMap(),
            fcmToken = fcmToken,
            clientIp = clientIp,
            statusCode = response.status,
            executionTime = executionTime
        )

        // ApiLogPersistenceAdapter.save(apiLog)
    }

    private fun getClientIp(request: HttpServletRequest): String =
        request.getHeader("X-Forwarded-For")?.substringBefore(",") ?: request.remoteAddr

}