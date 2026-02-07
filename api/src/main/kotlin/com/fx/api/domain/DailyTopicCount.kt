package com.fx.api.domain

import java.time.LocalDate

data class DailyTopicCount(
    val date: LocalDate,
    val topic: String,
    val count: Long
)