package com.fx.api.config.security

import com.fx.api.exception.AuthException
import com.fx.api.exception.errorcode.AuthErrorCode
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.web.servlet.HandlerExceptionResolver

/**
 * 보안 필터 체인의 인증 · 권한 오류를 [AuthException] 으로 바꿔 `@RestControllerAdvice` 에 맡긴다.
 * 컨트롤러 오류와 같은 `Api` 형식 응답이 된다.
 *
 * - 인증 정보가 없으면 401. 토큰이 있었는데 인증하지 못했으면 그 이유(만료 · 잘못된 토큰)로 응답한다.
 * - 인증했지만 권한이 없으면 403.
 */
class AuthErrorResponder(
    private val handlerExceptionResolver: HandlerExceptionResolver,
) : AuthenticationEntryPoint, AccessDeniedHandler {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val errorCode = request.getAttribute(JwtAuthenticationFilter.AUTH_ERROR_ATTRIBUTE) as? AuthErrorCode
            ?: AuthErrorCode.UNAUTHORIZED
        respond(request, response, errorCode)
    }

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        respond(request, response, AuthErrorCode.FORBIDDEN)
    }

    private fun respond(request: HttpServletRequest, response: HttpServletResponse, errorCode: AuthErrorCode) {
        handlerExceptionResolver.resolveException(request, response, null, AuthException(errorCode))
    }

}
