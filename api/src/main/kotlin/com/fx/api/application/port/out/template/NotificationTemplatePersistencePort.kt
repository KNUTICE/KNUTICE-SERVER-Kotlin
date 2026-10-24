package com.fx.api.application.port.out.template

import com.fx.common.domain.notification.NotificationTemplate

interface NotificationTemplatePersistencePort {

    /** 등록 순. */
    fun findAll(): List<NotificationTemplate>

}
