package com.fx.api.application.service.template

import com.fx.api.application.port.`in`.template.NotificationTemplateQueryUseCase
import com.fx.api.application.port.out.template.NotificationTemplatePersistencePort
import com.fx.common.domain.notification.NotificationTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** 알림 문구 관리자 조회. 카탈로그 캐시가 아니라 DB 를 읽어 방금 바꾼 문구도 보이게 한다. */
@Service
@Transactional(readOnly = true)
class NotificationTemplateQueryService(
    private val notificationTemplatePersistencePort: NotificationTemplatePersistencePort,
) : NotificationTemplateQueryUseCase {

    override fun getTemplates(): List<NotificationTemplate> =
        notificationTemplatePersistencePort.findAll()

}
