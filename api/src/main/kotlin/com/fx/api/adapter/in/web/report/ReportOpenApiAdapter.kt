package com.fx.api.adapter.`in`.web.report

import com.fx.api.adapter.`in`.web.report.dto.ReportSaveRequest
import com.fx.api.application.port.`in`.report.ReportCommandUseCase
import com.fx.common.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/open-api/v1/reports")
class ReportOpenApiAdapter(
    private val reportCommandUseCase: ReportCommandUseCase
) : ReportOpenApiSwagger {

    @PostMapping
    override fun saveReport(
        @RequestHeader fcmToken: String,
        @RequestBody @Valid reportSaveRequest: ReportSaveRequest
    ): ResponseEntity<Api<Boolean>> =
        Api.OK(reportCommandUseCase.saveReport(reportSaveRequest.toCommand(fcmToken)))

}
