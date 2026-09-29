package com.fx.common.application.port.`in`

import com.fx.common.domain.catalog.NotificationTemplateCatalog
import com.fx.common.domain.catalog.TopicCatalog

interface CatalogQueryUseCase {

    fun getTopicCatalog(): TopicCatalog

    fun getNotificationTemplateCatalog(): NotificationTemplateCatalog

    /**
     * 캐시된 스냅샷을 버린다. 다음 조회 때 DB 에서 다시 읽는다.
     * 관리자가 토픽 · 단과대 · 알림 문구를 바꾼 뒤, 또는 배치 Job 을 시작할 때 부른다.
     */
    fun refresh()

}
