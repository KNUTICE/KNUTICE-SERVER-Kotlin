package com.fx.api.adapter.out.persistence

import com.fx.api.application.port.out.template.NotificationTemplatePersistencePort
import com.fx.common.adapter.out.persistence.repository.NotificationTemplateRepository
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.domain.notification.NotificationTemplate

@PersistenceAdapter
class NotificationTemplatePersistenceAdapter(
    private val notificationTemplateRepository: NotificationTemplateRepository,
) : NotificationTemplatePersistencePort {

    override fun findAll(): List<NotificationTemplate> =
        notificationTemplateRepository.findAllByOrderByIdAsc()

}
