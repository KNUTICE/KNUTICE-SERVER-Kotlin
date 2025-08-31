package com.fx.api.application.port.`in`.dto

data class TopicUpdateCommand (
    val fcmToken: String,
    val subscribedTopics: Set<String>
)