package com.fx.api.adapter.`in`.web.apilog

import com.fx.api.adapter.`in`.web.apilog.dto.ApiLogResponse
import com.fx.api.application.port.`in`.apilog.ApiLogQueryUseCase
import com.fx.api.domain.ApiLogStatusGroup
import com.fx.common.annotation.hexagonal.WebInputAdapter
import com.fx.persistence.request.PagingRequest
import com.fx.persistence.response.PageResponse
import io.github.seob7.Api
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

@WebInputAdapter
@RequestMapping("/api/v1/statistics/api-logs")
class ApiLogApiAdapter(
    private val apiLogQueryUseCase: ApiLogQueryUseCase,
) : ApiLogApiSwagger {

    @GetMapping
    override fun getApiLogs(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
        @RequestParam(required = false) urlPattern: String?,
        @RequestParam(required = false) statusGroup: ApiLogStatusGroup?,
        @Valid @ParameterObject pagingRequest: PagingRequest,
    ): ResponseEntity<Api<PageResponse<ApiLogResponse>>> {
        val logPage = apiLogQueryUseCase.getApiLogs(date, urlPattern, statusGroup, pagingRequest.toPageable())
        return Api.OK(
            PageResponse.from(logPage) {
                ApiLogResponse.from(it)
            },
            "API 로그 조회 성공"
        )
    }

}
