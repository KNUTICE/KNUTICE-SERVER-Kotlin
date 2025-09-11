package com.fx.api.domain

import com.fx.api.application.port.`in`.dto.ReportSaveCommand
import java.time.LocalDateTime

data class Report(
    val id: String ?= null,
    val fcmToken: String,
    val content: String,
    val deviceName: String,
    val version: String,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
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
