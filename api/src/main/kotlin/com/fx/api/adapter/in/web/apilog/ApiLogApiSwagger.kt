package com.fx.api.adapter.`in`.web.apilog

import com.fx.api.adapter.`in`.web.apilog.dto.ApiLogResponse
import com.fx.api.domain.ApiLogStatusGroup
import com.fx.persistence.request.PagingRequest
import com.fx.persistence.response.PageResponse
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

@Tag(name = "API 로그 API - ADMIN")
interface ApiLogApiSwagger {

    @Operation(summary = "원본 API 로그 목록", description = "앱 Open API 요청 기록을 하루 단위로, 최신순으로 페이지 단위 조회합니다. page 는 1부터, size 는 1~100(기본 20)입니다.<br>" +
            "보관 기간이 지난 날짜는 비어 있습니다. 오늘 로그는 페이지를 넘기는 사이 새 요청이 앞에 추가돼 몇 건이 겹쳐 보일 수 있습니다.")
    fun getApiLogs(
        @Parameter(description = "날짜 (yyyy-MM-dd, KST). 없으면 오늘") @RequestParam(required = false) date: LocalDate?,
        @Parameter(description = "URL 패턴 (예: /open-api/v1/notices/{nttId}). 없으면 모든 URL") @RequestParam(required = false) urlPattern: String?,
        @Parameter(description = "CLIENT_ERROR(4xx) · SERVER_ERROR(5xx). 없으면 모든 상태") @RequestParam(required = false) statusGroup: ApiLogStatusGroup?,
        @ParameterObject pagingRequest: PagingRequest,
    ): ResponseEntity<Api<PageResponse<ApiLogResponse>>>

}
