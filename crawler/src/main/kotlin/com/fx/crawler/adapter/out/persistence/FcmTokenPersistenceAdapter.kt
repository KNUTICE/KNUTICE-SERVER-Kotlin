package com.fx.crawler.adapter.out.persistence

import com.fx.global.adapter.out.persistence.document.FcmTokenDocument
import com.fx.crawler.adapter.out.persistence.repository.FcmTokenMongoRepository
import com.fx.crawler.adapter.out.persistence.repository.FcmTokenQueryRepository
import com.fx.crawler.appllication.port.out.FcmTokenPersistencePort
import com.fx.global.domain.FcmToken
import com.fx.crawler.domain.FcmTokenQuery
import com.fx.global.annotation.PersistenceAdapter

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

    override fun findByFcmToken(fcmToken: String): FcmToken?  {
        return fcmTokenMongoRepository.findById(fcmToken).orElse(null)?.toDomain()
    }

    override fun findByCreatedAtAndIsActive(fcmTokenQuery: FcmTokenQuery): List<FcmToken> =
        fcmTokenQueryRepository.findByCreatedAtAndIsActive(fcmTokenQuery).map { it.toDomain() };


}