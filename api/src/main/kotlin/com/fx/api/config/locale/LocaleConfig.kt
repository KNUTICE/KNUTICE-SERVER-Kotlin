package com.fx.api.config.locale

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import java.util.Locale

/**
 * 국제화 기본 설정 Bean
 *
 * @author 이동섭
 * @since 2026-04-18
 */
@Configuration
class LocaleConfig {

    @Bean
    fun localeResolver(): LocaleResolver {
        val resolver = AcceptHeaderLocaleResolver().apply {
            setSupportedLocales(
                listOf(
                    Locale.ENGLISH, // en-US
                    Locale.KOREAN, // ko-KR
                    Locale.JAPANESE, // ja-JA
                )
            )
            setDefaultLocale(Locale.KOREAN) // Default Locale 설정
        }
        return resolver
    }

}