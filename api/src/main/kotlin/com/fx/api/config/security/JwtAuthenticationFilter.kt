package com.fx.api.config.security

import com.fx.api.application.port.out.user.JwtProviderPort
import com.fx.api.config.security.dto.AuthenticatedUser
import com.fx.api.exception.AuthException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 관리자 API(`/api/` 로 시작하는 경로) 요청의 액세스 토큰으로 인증 정보를 채운다.
 *
 * 토큰이 없거나 잘못됐으면 인증 없이 다음 필터로 넘긴다. 인가에서 거절되면 [AuthErrorResponder] 가
 * [AUTH_ERROR_ATTRIBUTE] 에 남긴 이유(만료 · 잘못된 토큰)로 응답한다. 필터에서 던진 예외는 `@RestControllerAdvice` 에 닿지 않는다.
 *
 * 빈으로 등록하면 Spring Boot 가 서블릿 필터로도 등록해 보안 필터 체인 밖에서 한 번 더 실행되므로 [SecurityConfig] 에서 직접 만든다.
 */
class JwtAuthenticationFilter(
    private val jwtProviderPort: JwtProviderPort,
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        !request.requestURI.startsWith(ADMIN_API_PREFIX)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = request.getHeader(HttpHeaders.AUTHORIZATION)
            ?.takeIf {
                it.startsWith(BEARER_PREFIX)
            }
            ?.removePrefix(BEARER_PREFIX)
            ?.trim()

        if (!token.isNullOrEmpty()) {
            try {
                authenticate(token)
            } catch (e: AuthException) {
                request.setAttribute(AUTH_ERROR_ATTRIBUTE, e.baseErrorCode)
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun authenticate(token: String) {
        val authenticatedUserInfo = jwtProviderPort.authenticate(token)
        val user = AuthenticatedUser(
            userId = authenticatedUserInfo.userId,
            role = authenticatedUserInfo.role,
        )
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = UsernamePasswordAuthenticationToken.authenticated(user, null, user.authorities)
        SecurityContextHolder.setContext(context)
    }

    companion object {

        /** 인증하지 못한 이유 (`AuthErrorCode`) */
        val AUTH_ERROR_ATTRIBUTE: String = JwtAuthenticationFilter::class.java.name + ".AUTH_ERROR"

        private const val ADMIN_API_PREFIX = "/api/"
        private const val BEARER_PREFIX = "Bearer "

    }

}
