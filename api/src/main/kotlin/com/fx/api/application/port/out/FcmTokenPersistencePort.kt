package com.fx.api.application.port.out

import com.fx.global.domain.DeviceType
import com.fx.global.domain.FcmToken

interface FcmTokenPersistencePort {

    fun saveFcmToken(fcmToken: FcmToken)

    fun findByFcmToken(fcmToken: String): FcmToken?

    fun existsByFcmToken(fcmToken: String): Boolean

    fun countByIsActiveAndDeviceType(isActive: Boolean, deviceType: DeviceType): Long

}