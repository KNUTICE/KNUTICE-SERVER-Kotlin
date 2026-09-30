package com.fx.api.application.service.topic

import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.domain.TopicType
import com.fx.common.domain.catalog.TopicView
import com.fx.common.exception.TopicException
import com.fx.common.exception.errorcode.TopicErrorCode
import org.springframework.stereotype.Component

/**
 * 요청의 토픽 식별자(v1 이름 · v2 코드)를 토픽 카탈로그에서 찾는다.
 * 없는(또는 삭제된) 토픽이거나 기대한 유형이 아니면 [TopicException] 을 던진다.
 */
@Component
class TopicResolver(
    private val catalogQueryUseCase: CatalogQueryUseCase,
) {

    fun byName(topicName: String, expectedType: TopicType? = null): TopicView {
        val topic = catalogQueryUseCase.getTopicCatalog().findByName(topicName)
        return topic?.takeIf { expectedType == null || it.topicType == expectedType }
            ?: throw TopicException(TopicErrorCode.TOPIC_NOT_FOUND)
    }

    fun byCode(topicId: Int): TopicView =
        catalogQueryUseCase.getTopicCatalog().findByCode(topicId)
            ?: throw TopicException(TopicErrorCode.TOPIC_NOT_FOUND)

    /** [topicId] 가 있으면 코드로, 없으면 [topicName] 으로 찾는다. 둘 다 없으면 null. */
    fun byNameOrCode(topicName: String?, topicId: Int?, expectedTypeForName: TopicType? = null): TopicView? =
        when {
            topicId != null -> byCode(topicId)
            topicName != null -> byName(topicName, expectedTypeForName)
            else -> null
        }

}
