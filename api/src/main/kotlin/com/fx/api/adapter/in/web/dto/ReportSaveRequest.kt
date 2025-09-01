package com.fx.api.adapter.`in`.web.dto

import com.fx.api.application.port.`in`.dto.ReportCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ReportSaveRequest(

    @field:NotBlank
    val fcmToken: String,

    @field:Size(min = 5, max = 500)
    val content: String,

    @field:NotBlank
    val deviceName: String,

    @field:NotBlank
    val version: String

) {
    fun toCommand(): ReportCommand =
        ReportCommand(
            fcmToken = this.fcmToken,
            content = this.content,
            deviceName = this.deviceName,
            version = this.version
        )
}