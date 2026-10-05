package com.fx.api.application.port.`in`.fcmtoken.dto

import com.fx.common.domain.i18n.Language

data class FcmTokenLanguageUpdateCommand(
    val fcmToken: String,
    val language: Language,
)
