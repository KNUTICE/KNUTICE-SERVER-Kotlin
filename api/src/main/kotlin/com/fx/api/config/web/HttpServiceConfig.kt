package com.fx.api.config.web

import com.fx.api.adapter.out.web.client.NotificationClient
import org.springframework.context.annotation.Configuration
import org.springframework.web.service.registry.ImportHttpServices

/**
 * HTTP Interface Client 등록
 * 그룹별 base-url · timeout 은 `spring.http.serviceclient.<group>.*` 로 설정한다.
 */
@Configuration
@ImportHttpServices(group = HttpServiceConfig.CRAWLER, types = [NotificationClient::class])
class HttpServiceConfig {

    companion object {
        const val CRAWLER = "crawler"
    }

}
