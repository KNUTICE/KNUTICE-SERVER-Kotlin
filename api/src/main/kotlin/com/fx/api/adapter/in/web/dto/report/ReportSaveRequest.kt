package com.fx.api.adapter.`in`.web.dto.report

import com.fx.api.application.port.`in`.dto.ReportSaveCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ReportSaveRequest(

    @field:Size(min = 5, max = 500)
    val content: String,

    @field:NotBlank
    val deviceName: String,

    @field:NotBlank
    val version: String

) {
    fun toCommand(fcmToken: String): ReportSaveCommand =
        ReportSaveCommand(
            fcmToken = fcmToken,
            content = this.content,
            deviceName = this.deviceName,
            version = this.version
        )
}