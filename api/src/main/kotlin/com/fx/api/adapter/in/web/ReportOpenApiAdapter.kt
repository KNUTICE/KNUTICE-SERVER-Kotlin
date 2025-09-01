package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.ReportRequest
import com.fx.api.adapter.`in`.web.swagger.ReportOpenApiSwagger
import com.fx.api.application.port.`in`.ReportCommandUseCase
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.api.Api
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/open-api/v1/reports")
class ReportOpenApiAdapter(
    private val reportCommandUseCase: ReportCommandUseCase
) : ReportOpenApiSwagger {

    // TODO Header Token
    @PostMapping
    override fun saveReport(
        @RequestBody reportRequest: ReportRequest
    ): ResponseEntity<Api<Boolean>> =
        Api.OK(reportCommandUseCase.saveReport(reportRequest.toCommand()))

}