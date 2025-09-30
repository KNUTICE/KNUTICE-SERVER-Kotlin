package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.metrics.MetricsResponse
import com.fx.global.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@WebInputAdapter
@RequestMapping("/api/v1/metrics")
class MetricsWebAdapter(
    private val meterRegistry: MeterRegistry
) {

    @GetMapping
    fun getRequestCount(@RequestParam requestUri: String): ResponseEntity<Api<MetricsResponse>> {
        val meter = meterRegistry
            .find("http.server.requests")
            .tags("uri", requestUri)

        val count = meter.counter()?.count()
            ?: meter.timer()?.count()?.toDouble()
            ?: meter.functionCounter()?.count()
            ?: 0.0

        return Api.OK(MetricsResponse(requestUri, count))
    }

    @GetMapping("/all")
    fun getAllRequestCountsSorted(): ResponseEntity<Api<List<MetricsResponse>>> {
        val allMetrics = meterRegistry
            .find("http.server.requests")
            .meters()
            .mapNotNull { meter ->
                val uri = meter.id.getTag("uri") ?: return@mapNotNull null
                val count = meter.measure().firstOrNull { it.statistic.name == "COUNT" }?.value
                    ?: 0.0
                MetricsResponse(uri, count)
            }
            .sortedByDescending { it.count }

        return Api.OK(allMetrics)
    }

}