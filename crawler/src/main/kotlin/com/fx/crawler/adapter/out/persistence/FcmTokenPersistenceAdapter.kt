package com.fx.crawler.adapter.out.persistence

import adapter.out.persistence.document.FcmTokenDocument
import adapter.out.persistence.document.NoticeDocument
import adapter.out.persistence.repository.FcmTokenMongoRepository
import adapter.out.persistence.repository.NoticeMongoRepository
import com.fx.crawler.appllication.port.out.NoticePersistencePort
import com.fx.crawler.domain.FcmToken
import com.fx.crawler.domain.Notice
import org.springframework.stereotype.Component

@Component
class FcmTokenPersistenceAdapter(
    private val fcmTokenMongoRepository: FcmTokenMongoRepository
) {

    fun save(fcmToken: FcmToken) {
        fcmTokenMongoRepository.save(FcmTokenDocument.from(fcmToken))
    }

}