package com.fx.api.config.converter

import com.fx.common.domain.CrawlableType
import com.fx.common.domain.MajorType
import com.fx.common.domain.MealType
import com.fx.common.domain.NoticeType
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
        MealType.entries.forEach { CrawlableType.register(it) }
    }

}