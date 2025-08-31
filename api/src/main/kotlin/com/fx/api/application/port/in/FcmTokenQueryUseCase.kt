package com.fx.api.application.port.`in`

import com.fx.global.domain.FcmToken

interface FcmTokenQueryUseCase {

    fun getMyTokenInfo(fcmToken: String): FcmToken

}