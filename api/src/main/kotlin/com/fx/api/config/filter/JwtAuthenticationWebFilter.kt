package com.fx.api.config.filter

import com.fx.api.application.port.out.JwtProviderPort
import com.fx.global.annotation.SecurityAdapter
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@SecurityAdapter

class JwtAuthenticationWebFilter(
    private val jwtProviderPort: JwtProviderPort
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.path.value()
        if (!path.startsWith("/api/")) {
            return chain.filter(exchange)
        }

        val header = exchange.request.headers.getFirst("Authorization")
        if (!header.isNullOrEmpty() && header.startsWith("Bearer ")) {
            val token = header.removePrefix("Bearer ").trim()
            if (jwtProviderPort.validateToken(token)) {
                val userInfo = jwtProviderPort.getAuthenticatedUserInfo(token)
                val authentication = UsernamePasswordAuthenticationToken(
                    userInfo,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_${userInfo.role}"))
                )

                return chain.filter(exchange)
                    .contextWrite(
                        ReactiveSecurityContextHolder.withAuthentication(authentication)
                    )
            }
        }

        return chain.filter(exchange)
    }
}