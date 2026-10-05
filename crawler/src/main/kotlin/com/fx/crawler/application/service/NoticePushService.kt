package com.fx.crawler.application.service

import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.domain.TopicType
import com.fx.crawler.application.port.`in`.NoticePushUseCase
import com.fx.crawler.application.port.out.NoticePersistencePort
import com.fx.crawler.domain.push.NoticePushComposer
import com.fx.crawler.domain.push.TopicPushPlan
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class NoticePushService(
    private val catalogQueryUseCase: CatalogQueryUseCase,
    private val noticePersistencePort: NoticePersistencePort,
    private val clock: Clock,
) : NoticePushUseCase {

    /**
     * 이번 실행에서 저장한 공지뿐 아니라, 이전 실행이 발송 전에 멈춰 남아 있던 공지도 함께 보낸다.
     * 삭제된 토픽의 공지는 표시명이 없으므로 보내지 않는다.
     */
    override fun preparePushPlans(topicType: TopicType): List<TopicPushPlan> {
        val topicsByCode = catalogQueryUseCase.getTopicCatalog().topicsOf(topicType).associateBy {
            it.code
        }
        if (topicsByCode.isEmpty()) {
            return emptyList()
        }
        val templates = catalogQueryUseCase.getNotificationTemplateCatalog()

        return noticePersistencePort.findPendingNotification(topicsByCode.keys)
            .groupBy {
                it.topicCode
            }
            .map { (topicCode, notices) ->
                TopicPushPlan(
                    topicCode = topicCode,
                    messages = NoticePushComposer.compose(topicsByCode.getValue(topicCode), notices, templates),
                    noticeIds = notices.map {
                        requireNotNull(it.id)
                    },
                )
            }
    }

    @Transactional
    override fun markNotified(noticeIds: List<Long>) {
        if (noticeIds.isNotEmpty()) {
            noticePersistencePort.markNotified(noticeIds, LocalDateTime.now(clock))
        }
    }

}
