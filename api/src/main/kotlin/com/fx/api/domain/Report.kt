package com.fx.api.domain

import com.fx.api.application.port.`in`.dto.ReportSaveCommand

data class Report(
    val fcmToken: String,
    val content: String,
    val deviceName: String,
    val version: String
) {
    companion object {
        fun createReport(reportSaveCommand: ReportSaveCommand): Report =
            Report(
                fcmToken = reportSaveCommand.fcmToken,
                content = reportSaveCommand.content,
                deviceName = reportSaveCommand.deviceName,
                version = reportSaveCommand.version
            )
    }
}
