package com.fx.crawler.adapter.out.persistence

import com.fx.common.adapter.out.persistence.document.FcmTokenDocument
import com.fx.crawler.adapter.out.persistence.repository.FcmTokenQueryRepository
import com.fx.crawler.appllication.port.out.FcmTokenPersistencePort
import com.fx.common.domain.FcmToken
import com.fx.crawler.domain.FcmTokenQuery
import com.fx.common.adapter.out.persistence.repository.FcmTokenMongoRepository
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.domain.DeviceType

@PersistenceAdapter
class FcmTokenPersistenceAdapter(
    private val fcmTokenMongoRepository: FcmTokenMongoRepository,
    private val fcmTokenQueryRepository: FcmTokenQueryRepository
): FcmTokenPersistencePort {

    override fun save(fcmToken: FcmToken) {
        fcmTokenMongoRepository.save(FcmTokenDocument.from(fcmToken))
    }

    override fun saveAll(fcmTokens: List<FcmToken>) {
        fcmTokenMongoRepository.saveAll(FcmTokenDocument.from(fcmTokens))
    }

    override fun findByFcmToken(fcmToken: String): FcmToken?  =
        fcmTokenMongoRepository.findById(fcmToken).orElse(null)?.toDomain()

    override fun findByCreatedAtAndIsActive(fcmTokenQuery: FcmTokenQuery): List<FcmToken> =
        fcmTokenQueryRepository.findByCreatedAtAndIsActive(fcmTokenQuery).map { it.toDomain() };

    override fun countByIsActiveAndDeviceType(isActive: Boolean, deviceType: DeviceType): Long =
        fcmTokenMongoRepository.countByIsActiveAndDeviceType(isActive, deviceType)

}