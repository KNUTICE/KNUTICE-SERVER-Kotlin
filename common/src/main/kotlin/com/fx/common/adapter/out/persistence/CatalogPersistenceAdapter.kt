package com.fx.common.adapter.out.persistence

import com.fx.common.adapter.out.persistence.repository.CollegeRepository
import com.fx.common.adapter.out.persistence.repository.NotificationTemplateRepository
import com.fx.common.adapter.out.persistence.repository.TopicRepository
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.application.port.out.CatalogPersistencePort
import com.fx.common.domain.catalog.CollegeView
import com.fx.common.domain.catalog.NotificationTemplateCatalog
import com.fx.common.domain.catalog.TopicCatalog
import com.fx.common.domain.catalog.TopicView
import org.springframework.transaction.annotation.Transactional

/**
 * 카탈로그 스냅샷을 만든다. 엔티티는 트랜잭션 밖으로 내보내지 않고 읽기 모델로 옮겨 담는다.
 * 토픽과 단과대를 같은 트랜잭션에서 읽어 서로 어긋난 스냅샷이 만들어지지 않게 한다.
 */
@PersistenceAdapter
class CatalogPersistenceAdapter(
    private val topicRepository: TopicRepository,
    private val collegeRepository: CollegeRepository,
    private val notificationTemplateRepository: NotificationTemplateRepository,
) : CatalogPersistencePort {

    @Transactional(readOnly = true)
    override fun loadTopicCatalog(): TopicCatalog {
        val colleges = collegeRepository.findAllByDeletedAtIsNull().map {
            CollegeView(
                id = requireNotNull(it.id),
                collegeKey = it.collegeKey,
                displayName = it.displayName.copy(),
                displayOrder = it.displayOrder,
            )
        }
        val collegesById = colleges.associateBy { it.id }

        val topics = topicRepository.findAllByDeletedAtIsNull().map {
            TopicView(
                id = requireNotNull(it.id),
                code = it.code,
                name = it.name,
                topicType = it.topicType,
                displayName = it.displayName.copy(),
                college = it.collegeId?.let(collegesById::get),
                rootDomain = it.rootDomain,
                bbsPath = it.bbsPath,
                crawlEnabled = it.crawlEnabled,
                visible = it.visible,
            )
        }

        return TopicCatalog(topics, colleges)
    }

    @Transactional(readOnly = true)
    override fun loadNotificationTemplateCatalog(): NotificationTemplateCatalog =
        NotificationTemplateCatalog(
            notificationTemplateRepository.findAll().associate { it.templateKey to it.text.copy() }
        )

}
