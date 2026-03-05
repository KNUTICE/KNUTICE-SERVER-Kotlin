package com.fx.api.config.webclient

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig(
    @Value("\${crawler.server.address}")
    private val crawlerBaseUrl: String
) {

    @Bean
    fun crawlerWebClient(): WebClient =
        WebClient.builder()
            .baseUrl(crawlerBaseUrl)
            .build()

}