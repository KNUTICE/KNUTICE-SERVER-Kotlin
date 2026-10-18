package com.fx.api.adapter.`in`.web.user

import com.fx.api.adapter.`in`.web.user.dto.TokenResponse
import com.fx.api.adapter.`in`.web.user.dto.UserIdResponse
import com.fx.api.adapter.`in`.web.user.dto.UserLoginRequest
import com.fx.api.adapter.`in`.web.user.dto.UserSignUpRequest
import com.fx.api.application.port.`in`.user.UserCommandUseCase
import com.fx.api.domain.TokenInfo
import com.fx.api.exception.AuthException
import com.fx.api.exception.errorcode.AuthErrorCode
import com.fx.common.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/open-api/v1/users")
class UserOpenApiAdapter(
    private val userCommandUseCase: UserCommandUseCase,
    private val refreshTokenCookies: RefreshTokenCookies,
) : UserOpenApiSwagger {

    @PostMapping("/signup")
    override fun signUp(
        @RequestBody @Valid signUpRequest: UserSignUpRequest
    ): ResponseEntity<Api<UserIdResponse>> =
        Api.OK(UserIdResponse(
            userCommandUseCase.signUp(signUpRequest.toCommand()).id?.toString()),
            "회원가입이 완료되었습니다.",
            HttpStatus.CREATED,
        )

    @PostMapping("/login")
    override fun login(
        @RequestBody @Valid loginRequest: UserLoginRequest
    ): ResponseEntity<Api<TokenResponse>> =
        tokenResponse(userCommandUseCase.login(loginRequest.toCommand()), "로그인되었습니다.")

    @PostMapping("/refresh")
    override fun refresh(
        @CookieValue(name = RefreshTokenCookies.NAME, required = false) refreshToken: String?,
    ): ResponseEntity<Api<TokenResponse>> {
        if (refreshToken.isNullOrBlank()) {
            throw AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN)
        }
        return tokenResponse(userCommandUseCase.refresh(refreshToken), "토큰을 다시 발급했습니다.")
    }

    /** 세션이 이미 없어도 쿠키를 지우고 성공으로 응답한다. */
    @PostMapping("/logout")
    override fun logout(
        @CookieValue(name = RefreshTokenCookies.NAME, required = false) refreshToken: String?,
    ): ResponseEntity<Api<Boolean>> {
        if (!refreshToken.isNullOrBlank()) {
            userCommandUseCase.logout(refreshToken)
        }
        return Api.OK(true, "로그아웃되었습니다.")
            .withCookie(refreshTokenCookies.expire())
    }

    private fun tokenResponse(tokenInfo: TokenInfo, message: String): ResponseEntity<Api<TokenResponse>> =
        Api.OK(TokenResponse.from(tokenInfo), message)
            .withCookie(refreshTokenCookies.create(tokenInfo.refreshToken))

    private fun <T> ResponseEntity<Api<T>>.withCookie(cookie: ResponseCookie): ResponseEntity<Api<T>> =
        ResponseEntity.status(statusCode)
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(body)

}
