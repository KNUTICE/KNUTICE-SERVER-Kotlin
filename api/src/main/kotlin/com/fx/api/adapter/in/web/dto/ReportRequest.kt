package com.fx.api.adapter.`in`.web.dto

import com.fx.api.application.port.`in`.dto.ReportCommand
import jakarta.validation.constraints.NotBlank

data class ReportRequest(

    @NotBlank
    val fcmToken: String,
    val content: String,
    val deviceName: String,
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