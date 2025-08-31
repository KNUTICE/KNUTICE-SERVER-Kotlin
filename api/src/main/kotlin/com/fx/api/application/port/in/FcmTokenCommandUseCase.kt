package com.fx.api.application.port.`in`

import com.fx.api.application.port.`in`.dto.FcmTokenCommand

interface FcmTokenCommandUseCase {

    fun saveFcmToken(fcmTokenCommand: FcmTokenCommand): Boolean

}