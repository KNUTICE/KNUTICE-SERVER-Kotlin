package com.fx.crawler.appllication.port.out

import com.fx.global.domain.FcmToken
import com.fx.crawler.domain.FcmTokenQuery
import com.fx.global.domain.DeviceType

interface FcmTokenPersistencePort {

    fun findByCreatedAtAndIsActive(fcmTokenQuery: FcmTokenQuery): List<FcmToken>

    fun findByFcmToken(fcmToken: String): FcmToken?

    fun save(fcmToken: FcmToken)

    fun saveAll(fcmTokens: List<FcmToken>)

    fun countByIsActiveAndDeviceType(isActive: Boolean, deviceType: DeviceType): Long

}
