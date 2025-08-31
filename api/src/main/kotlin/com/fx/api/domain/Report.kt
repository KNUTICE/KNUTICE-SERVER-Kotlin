package com.fx.api.domain

import com.fx.api.application.port.`in`.dto.ReportCommand

data class Report(
    val fcmToken: String,
    val content: String,
    val deviceName: String,
    val version: String
) {
    companion object {
        fun createReport(reportCommand: ReportCommand): Report =
            Report(
                fcmToken = reportCommand.fcmToken,
                content = reportCommand.content,
                deviceName = reportCommand.deviceName,
                version = reportCommand.version
            )
    }
}
