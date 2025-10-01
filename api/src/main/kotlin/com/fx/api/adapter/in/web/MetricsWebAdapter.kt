package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.metrics.MetricsResponse
import com.fx.global.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import io.micrometer.core.instrument.MeterRegistry
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "모니터링")
@WebInputAdapter
@RequestMapping("/api/v1/metrics")
class MetricsWebAdapter(
    private val meterRegistry: MeterRegistry
) {

    @Operation(
        summary = "단일 API 요청 수 조회",
        description =
            "특정 URI에 대한 요청 횟수를 조회합니다. <br>" +
            "쿼리 파라미터가 있는 경우, `query` 파라미터를 전달하면 해당 쿼리 포함 요청만 집계합니다. <br>"
    )
    @GetMapping("/requests")
    fun getRequestCount(
        @RequestParam requestUri: String,
        @RequestParam(required = false) query: String?
    ): ResponseEntity<Api<MetricsResponse>> {

        val allMeters = meterRegistry.find("http.requests.with.params").meters()

        val filteredCount = allMeters
            .filter { meter ->
                val uri = meter.id.getTag("uri") ?: return@filter false
                val q = meter.id.getTag("query") ?: ""

                if (query.isNullOrEmpty()) {
                    uri == requestUri
                } else {
                    uri == requestUri && q.contains(query)
                }
            }
            .sumOf { meter ->
                meter.measure().firstOrNull { it.statistic.name == "COUNT" }?.value ?: 0.0
            }

        val displayUri = if (query.isNullOrEmpty()) requestUri else "$requestUri?$query"

        return Api.OK(MetricsResponse(displayUri, filteredCount))
    }

    @Operation(
        summary = "전체 API 요청 내림차순 조회",
        description =
            "모든 API 요청의 카운트를 조회하여 요청 수 기준 내림차순으로 반환합니다. <br>" +
            "쿼리 파라미터도 포함하여 각각 구분됩니다."
    )
    @GetMapping("/requests/all")
    fun getAllRequestCountsSorted(): ResponseEntity<Api<List<MetricsResponse>>> {
        val allMetrics = meterRegistry.find("http.requests.with.params").meters()
            .mapNotNull { meter ->
                val uri = meter.id.getTag("uri") ?: return@mapNotNull null
                val query = meter.id.getTag("query") ?: ""
                val count = meter.measure().firstOrNull { it.statistic.name == "COUNT" }?.value ?: 0.0
                MetricsResponse(
                    if (query.isNotEmpty()) "$uri?$query" else uri,
                    count
                )
            }
            .sortedByDescending { it.count }

        return Api.OK(allMetrics)
    }

}