package com.fx.api.config.converter

import com.fx.global.domain.CrawlableType
import com.fx.global.domain.MajorType
import com.fx.global.domain.NoticeType
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

@Configuration
class CrawlableTypeInitializer {

    /**
     * 새로운 타입이 추가될 때마다 XXXX.entries.forEach{} 추가
     * author: SEOB
     */
    @PostConstruct
    fun init() {
        NoticeType.entries.forEach { CrawlableType.register(it) }
        MajorType.entries.forEach { CrawlableType.register(it) }
    }

}