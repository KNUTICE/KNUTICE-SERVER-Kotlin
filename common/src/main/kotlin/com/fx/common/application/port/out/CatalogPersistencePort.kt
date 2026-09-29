package com.fx.common.application.port.out

import com.fx.common.domain.catalog.NotificationTemplateCatalog
import com.fx.common.domain.catalog.TopicCatalog

interface CatalogPersistencePort {

    /** 삭제되지 않은 토픽 · 단과대 전체를 읽어 스냅샷을 만든다. */
    fun loadTopicCatalog(): TopicCatalog

    /** 알림 문구 전체를 읽어 스냅샷을 만든다. */
    fun loadNotificationTemplateCatalog(): NotificationTemplateCatalog

}
