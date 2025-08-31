package com.fx.api.config.converter

import com.fx.global.domain.CrawlableType
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class CrawlableTypeConverter : Converter<String, CrawlableType> {
    override fun convert(source: String): CrawlableType {
        return CrawlableType.fromString(source)
    }
}