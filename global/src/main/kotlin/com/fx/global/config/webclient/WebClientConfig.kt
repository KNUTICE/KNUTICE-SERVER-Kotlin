package com.fx.global.config.webclient

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fx.global.exception.ConnectionException
import com.fx.global.exception.errorcode.ConnectionErrorCode
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Configuration
class WebClientConfig {

    @Bean
    fun webClient(): WebClient {
        val strategies = ExchangeStrategies.builder()
            .codecs { configurer ->
                val mapper = configurer.defaultCodecs().jackson2JsonDecoder(
                    Jackson2JsonDecoder(
                        jacksonObjectMapper()
                            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    )
                )
            }
            .build()

        return WebClient.builder()
            .exchangeStrategies(strategies)
            .filter(errorHandlingFilter())
            .build()
    }

    private fun errorHandlingFilter() = ExchangeFilterFunction.ofResponseProcessor { response ->
        if (response.statusCode().isError) {
            Mono.error(ConnectionException(ConnectionErrorCode.REMOTE_SERVER_UNAVAILABLE))
        } else {
            Mono.just(response)
        }
    }
}