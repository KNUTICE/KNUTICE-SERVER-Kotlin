package com.fx.api.adapter.`in`.web.user

import com.fx.api.adapter.`in`.web.user.dto.TokenResponse
import com.fx.api.adapter.`in`.web.user.dto.UserIdResponse
import com.fx.api.adapter.`in`.web.user.dto.UserLoginRequest
import com.fx.api.adapter.`in`.web.user.dto.UserSignUpRequest
import com.fx.api.exception.errorcode.AuthErrorCode
import com.fx.api.exception.errorcode.UserErrorCode
import com.fx.common.annotation.ApiExceptionExplanation
import com.fx.common.annotation.ApiResponseExplanations
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "USER API")
interface UserOpenApiSwagger {

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "회원가입 실패 - email",
                description = "email 이 이미 존재하는 경우",
                value = UserErrorCode::class,
                constant = "EMAIL_EXISTS"
            ),
            ApiExceptionExplanation(
                name = "회원가입 실패 - nickname",
                description = "nickname 이 이미 존재하는 경우",
                value = UserErrorCode::class,
                constant = "NICKNAME_EXISTS"
            )
        ]
    )
    @Operation(summary = "회원가입", description = "비밀번호는 영문과 숫자를 포함하여 5자 이상 20자 이하여야 합니다.<br>" +
            "닉네임은 2자 이상 30자 이하여야 합니다.")
    fun signUp(
        @RequestBody @Valid signUpRequest: UserSignUpRequest
    ): ResponseEntity<Api<UserIdResponse>>


    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "로그인 실패 - 1",
                description = "사용자를 찾을 수 없는 경우",
                value = UserErrorCode::class,
                constant = "USER_NOT_FOUND"
            ),
            ApiExceptionExplanation(
                name = "로그인 실패 - 2",
                description = "비밀번호가 일치하지 않는 경우",
                value = UserErrorCode::class,
                constant = "INVALID_PASSWORD"
            )
        ]
    )
    @Operation(summary = "로그인", description = "비밀번호는 영문과 숫자를 포함하여 5자 이상 20자 이하여야 합니다.<br>" +
            "액세스 토큰은 응답 본문으로, 리프레시 토큰은 HttpOnly 쿠키(`refresh_token`)로 전달합니다.")
    fun login(
        @RequestBody @Valid loginRequest: UserLoginRequest
    ): ResponseEntity<Api<TokenResponse>>

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "재발급 실패",
                description = "쿠키가 없거나, 만료됐거나, 이미 재발급에 쓴 리프레시 토큰인 경우. 이미 쓴 토큰이면 그 로그인 세션도 끊는다",
                value = AuthErrorCode::class,
                constant = "INVALID_REFRESH_TOKEN"
            )
        ]
    )
    @Operation(summary = "토큰 재발급", description = "리프레시 토큰 쿠키로 액세스 토큰을 다시 발급합니다.<br>" +
            "리프레시 토큰도 새것으로 바꿔 쿠키로 다시 전달하고, 만료를 연장합니다. 바뀌기 전 토큰은 더 쓸 수 없습니다.")
    fun refresh(
        @CookieValue(name = RefreshTokenCookies.NAME, required = false) refreshToken: String?,
    ): ResponseEntity<Api<TokenResponse>>

    @Operation(summary = "로그아웃", description = "리프레시 토큰 쿠키의 로그인 세션을 끝내고 쿠키를 지웁니다.<br>" +
            "이미 발급한 액세스 토큰은 만료될 때까지 유효하므로 클라이언트에서도 지워야 합니다.")
    fun logout(
        @CookieValue(name = RefreshTokenCookies.NAME, required = false) refreshToken: String?,
    ): ResponseEntity<Api<Boolean>>

}