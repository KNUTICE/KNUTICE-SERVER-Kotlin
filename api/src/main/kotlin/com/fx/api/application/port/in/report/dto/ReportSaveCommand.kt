package com.fx.api.application.port.`in`.report.dto

data class ReportSaveCommand(
    val fcmToken: String,
    val content: String,
    val deviceName: String,
    val version: String
)