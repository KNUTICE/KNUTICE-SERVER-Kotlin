package com.fx.common.adapter.out.persistence.repository

import com.fx.common.domain.notification.NotificationTemplate
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationTemplateRepository : JpaRepository<NotificationTemplate, Long>
