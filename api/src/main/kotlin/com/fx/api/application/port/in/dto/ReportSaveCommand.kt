package com.fx.api.application.port.`in`.dto

data class ReportSaveCommand(
    val fcmToken: String,
    val content: String,
    val deviceName: String,
    val version: String
)