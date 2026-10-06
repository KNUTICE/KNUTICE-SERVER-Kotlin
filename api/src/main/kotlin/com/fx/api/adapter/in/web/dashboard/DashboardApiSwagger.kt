package com.fx.api.adapter.`in`.web.dashboard

import com.fx.api.adapter.`in`.web.dashboard.dto.DashboardResponse
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity

@Tag(name = "대시보드 API - ADMIN")
interface DashboardApiSwagger {

    @Operation(summary = "대시보드", description = "관리자 첫 화면에 필요한 값을 한 번에 조회합니다.<br>" +
            "24시간 안의 실패 수 · 실행 중인 수 · 요약 대기 공지 수 · 꺼진 스케줄 수, 켜진 스케줄(다음 실행이 빠른 순), 최근 실행 8건")
    fun getDashboard(): ResponseEntity<Api<DashboardResponse>>

}
