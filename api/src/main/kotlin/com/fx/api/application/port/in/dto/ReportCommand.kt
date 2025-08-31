package com.fx.api.application.port.`in`.dto

data class ReportCommand(
    val fcmToken: String,
    val content: String,
    val deviceName: String,
    val version: String
)