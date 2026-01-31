package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.FcmTokenMongoRepository
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.global.adapter.out.persistence.document.FcmTokenDocument
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.DeviceType
import com.fx.global.domain.FcmToken

@PersistenceAdapter
class FcmTokenPersistenceAdapter(
    private val fcmTokenMongoRepository: FcmTokenMongoRepository
): FcmTokenPersistencePort {

    override fun saveFcmToken(fcmToken: FcmToken) {
        fcmTokenMongoRepository.save(FcmTokenDocument.from(fcmToken))
    }

    override fun findByFcmToken(fcmToken: String): FcmToken? =
        fcmTokenMongoRepository.findById(fcmToken).orElse(null)?.toDomain()

    override fun existsByFcmToken(fcmToken: String): Boolean =
        fcmTokenMongoRepository.existsById(fcmToken)

    override fun countByIsActiveAndDeviceType(isActive: Boolean, deviceType: DeviceType): Long =
        fcmTokenMongoRepository.countByIsActiveAndDeviceType(isActive, deviceType)

}