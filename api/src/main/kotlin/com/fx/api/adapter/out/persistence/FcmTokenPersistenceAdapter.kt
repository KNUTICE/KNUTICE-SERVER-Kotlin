package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.FcmTokenMongoRepository
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.global.adapter.out.persistence.document.FcmTokenDocument
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.DeviceType
import com.fx.global.domain.FcmToken

@PersistenceAdapter
class FcmTokenPersistenceAdapter(
    private val fcmTokenRepository: FcmTokenMongoRepository
): FcmTokenPersistencePort {

    override fun saveFcmToken(fcmToken: FcmToken) {
        fcmTokenRepository.save(FcmTokenDocument.from(fcmToken))
    }

    override fun findByFcmToken(fcmToken: String): FcmToken? =
        fcmTokenRepository.findById(fcmToken).orElse(null)?.toDomain()

    override fun existsByFcmToken(fcmToken: String): Boolean =
        fcmTokenRepository.existsById(fcmToken)

    override fun count(): Long =
        fcmTokenRepository.count()

    override fun countByIsActiveTrueAndDeviceType(deviceType: DeviceType): Long =
        fcmTokenRepository.countByIsActiveTrueAndDeviceType(deviceType)

}