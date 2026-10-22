package com.fx.api.application.port.`in`.template

import com.fx.common.domain.notification.NotificationTemplate

interface NotificationTemplateQueryUseCase {

    fun getTemplates(): List<NotificationTemplate>

}
