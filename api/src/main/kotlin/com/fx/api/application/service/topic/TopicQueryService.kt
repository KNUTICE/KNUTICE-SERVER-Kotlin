package com.fx.api.application.service.topic

import com.fx.api.application.port.`in`.topic.TopicQueryUseCase
import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.domain.TopicType
import com.fx.common.domain.catalog.TopicView
import com.fx.common.exception.TopicException
import com.fx.common.exception.errorcode.TopicErrorCode
import org.springframework.stereotype.Service

/** 토픽은 카탈로그(메모리 스냅샷)에서 읽으므로 트랜잭션을 걸지 않는다. */
@Service
class TopicQueryService(
    private val catalogQueryUseCase: CatalogQueryUseCase,
    private val topicResolver: TopicResolver,
) : TopicQueryUseCase {

    override fun getTopics(type: TopicType): List<TopicView> =
        catalogQueryUseCase.getTopicCatalog().topicsOf(type).filter {
            it.visible
        }

    override fun getTopic(topicName: String?, topicId: Int?): TopicView =
        topicResolver.byNameOrCode(topicName, topicId)
            ?: throw TopicException(TopicErrorCode.TOPIC_NOT_FOUND)

}
