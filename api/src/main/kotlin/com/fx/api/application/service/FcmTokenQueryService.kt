package com.fx.api.application.service

import com.fx.api.application.port.`in`.FcmTokenQueryUseCase
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.domain.TopicType
import com.fx.common.domain.catalog.TopicView
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FcmTokenQueryService(
    private val fcmTokenPersistencePort: FcmTokenPersistencePort,
    private val catalogQueryUseCase: CatalogQueryUseCase,
) : FcmTokenQueryUseCase {

    override fun getMyTopics(fcmToken: String, type: TopicType): List<TopicView> {
        val token = fcmTokenPersistencePort.getByToken(fcmToken)
        val subscribedCodes = fcmTokenPersistencePort.findSubscribedTopicCodes(requireNotNull(token.id))
        val catalog = catalogQueryUseCase.getTopicCatalog()

        // 삭제된 토픽의 구독은 카탈로그에 없으므로 빠진다
        return subscribedCodes
            .mapNotNull(catalog::findByCode)
            .filter { it.topicType == type }
            .sortedBy { it.code }
    }

}
