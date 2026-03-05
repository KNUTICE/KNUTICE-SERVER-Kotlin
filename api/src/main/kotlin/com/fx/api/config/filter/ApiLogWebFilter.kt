package com.fx.api.config.filter

import com.fx.api.application.port.`in`.ApiLogCommandUseCase
import com.fx.api.application.port.`in`.dto.ApiLogSaveCommand
import com.fx.global.domain.DeviceType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * API 요청 및 응답 로그 수집 필터
 *
 * @author 이동섭
 * @since 2026-02-06
 * @update
 * - 2026-03-03 : ApiLogInterceptor -> ApiLogWebFilter 로 변경, WebFlux 기반
 */
@Component
class ApiLogWebFilter(
    private val apiLogCommandUseCase: ApiLogCommandUseCase
) : WebFilter {

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain
    ): Mono<Void> {

        val startTime = System.currentTimeMillis()

        return chain.filter(exchange)
            .doOnSuccess {
                val executionTime = System.currentTimeMillis() - startTime

                val request = exchange.request
                val response = exchange.response

                val apiLogSaveCommand = ApiLogSaveCommand(
                    urlPattern = request.path.value(),
                    url = request.uri.path,
                    method = request.method.name(),
                    queryParameters = request.queryParams.toSingleValueMap(),
                    fcmToken = request.headers.getFirst("fcmToken"),
                    clientIp = request.remoteAddress?.address?.hostAddress ?: "",
                    deviceType = getDeviceType(request.headers.getFirst("User-Agent")),
                    statusCode = response.statusCode?.value() ?: 200,
                    executionTime = executionTime
                )

                apiLogCommandUseCase.recordApiLog(apiLogSaveCommand)
            }
    }

    private fun getDeviceType(userAgent: String?): DeviceType {
        val ua = userAgent?.lowercase() ?: ""
        return when {
            ua.contains("ios") -> DeviceType.iOS
            ua.contains("android") -> DeviceType.AOS
            else -> DeviceType.UNKNOWN
        }
    }
}