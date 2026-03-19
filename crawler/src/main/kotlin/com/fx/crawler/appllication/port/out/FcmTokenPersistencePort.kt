package com.fx.crawler.appllication.port.out

import com.fx.global.domain.FcmToken
import com.fx.crawler.domain.FcmTokenQuery
import com.fx.global.domain.DeviceType

interface FcmTokenPersistencePort {

    suspend fun findByCreatedAtAndIsActive(fcmTokenQuery: FcmTokenQuery): List<FcmToken>

    suspend fun findByFcmToken(fcmToken: String): FcmToken?

    suspend fun save(fcmToken: FcmToken)

    suspend fun saveAll(fcmTokens: List<FcmToken>)

    suspend fun countByIsActiveAndDeviceType(isActive: Boolean, deviceType: DeviceType): Long

}
