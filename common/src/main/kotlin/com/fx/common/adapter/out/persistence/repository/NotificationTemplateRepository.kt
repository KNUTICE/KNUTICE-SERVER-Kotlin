package com.fx.common.adapter.out.persistence.repository

import com.fx.common.domain.notification.NotificationTemplate
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationTemplateRepository : JpaRepository<NotificationTemplate, Long> {

    /** TSID 는 생성 순서를 보장하므로 id 오름차순 = 등록 순 */
    fun findAllByOrderByIdAsc(): List<NotificationTemplate>

}
