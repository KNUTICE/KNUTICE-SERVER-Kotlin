package com.fx.api.application.port.out

import com.fx.global.domain.FcmToken

interface FcmTokenPersistencePort {

    fun saveFcmToken(fcmToken: FcmToken)

    fun findByFcmToken(fcmToken: String): FcmToken?

}