package com.fx.api.domain

data class Statistics(
    val noticeCount: Long,
    val summaryContentCount: Long,
    val fcmTokenCount: Long,
    val aosActiveFcmTokenCount: Long,
    val iosActiveFcmTokenCount: Long
)
