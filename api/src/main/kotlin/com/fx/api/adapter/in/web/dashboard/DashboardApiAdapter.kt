package com.fx.api.adapter.`in`.web.dashboard

import com.fx.api.adapter.`in`.web.dashboard.dto.DashboardResponse
import com.fx.api.application.port.`in`.dashboard.DashboardQueryUseCase
import com.fx.common.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/api/v1/dashboard")
class DashboardApiAdapter(
    private val dashboardQueryUseCase: DashboardQueryUseCase,
) : DashboardApiSwagger {

    @GetMapping
    override fun getDashboard(): ResponseEntity<Api<DashboardResponse>> =
        Api.OK(DashboardResponse.from(dashboardQueryUseCase.getDashboard()), "대시보드 조회 성공")

}
