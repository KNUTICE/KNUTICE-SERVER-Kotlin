package com.fx.api.config.webflux

import org.springframework.context.annotation.Configuration
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver
import org.springframework.data.web.ReactiveSortHandlerMethodArgumentResolver
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer

@Configuration
class WebFluxPageableConfig : WebFluxConfigurer {

    override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
        // WebFlux 전용 Sort & Pageable 리졸버 수동 등록
        configurer.addCustomResolver(ReactiveSortHandlerMethodArgumentResolver())
        configurer.addCustomResolver(ReactivePageableHandlerMethodArgumentResolver())
    }
}