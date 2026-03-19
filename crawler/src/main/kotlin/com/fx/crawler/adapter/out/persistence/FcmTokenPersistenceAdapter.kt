package com.fx.crawler.adapter.out.persistence

import com.fx.global.adapter.out.persistence.document.FcmTokenDocument
import com.fx.crawler.adapter.out.persistence.repository.FcmTokenQueryRepository
import com.fx.crawler.appllication.port.out.FcmTokenPersistencePort
import com.fx.global.domain.FcmToken
import com.fx.crawler.domain.FcmTokenQuery
import com.fx.global.adapter.out.persistence.repository.FcmTokenMongoRepository
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.DeviceType
import kotlinx.coroutines.flow.collect

@PersistenceAdapter
class FcmTokenPersistenceAdapter(
    private val fcmTokenMongoRepository: FcmTokenMongoRepository,
    private val fcmTokenQueryRepository: FcmTokenQueryRepository
): FcmTokenPersistencePort {

    override suspend fun save(fcmToken: FcmToken) {
        fcmTokenMongoRepository.save(FcmTokenDocument.from(fcmToken))
    }

    override suspend fun saveAll(fcmTokens: List<FcmToken>) {
        fcmTokenMongoRepository.saveAll(FcmTokenDocument.from(fcmTokens)).collect()
    }

    override suspend fun findByFcmToken(fcmToken: String): FcmToken?  =
        fcmTokenMongoRepository.findById(fcmToken)?.toDomain()

    override suspend fun findByCreatedAtAndIsActive(fcmTokenQuery: FcmTokenQuery): List<FcmToken> =
        fcmTokenQueryRepository.findByCreatedAtAndIsActive(fcmTokenQuery).map { it.toDomain() };

    override suspend fun countByIsActiveAndDeviceType(isActive: Boolean, deviceType: DeviceType): Long =
        fcmTokenMongoRepository.countByIsActiveAndDeviceType(isActive, deviceType)

}