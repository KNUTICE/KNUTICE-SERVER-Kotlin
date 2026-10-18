package com.fx.api.application.port.`in`.user

import com.fx.api.application.port.`in`.user.dto.UserLoginCommand
import com.fx.api.application.port.`in`.user.dto.UserSignUpCommand
import com.fx.api.domain.TokenInfo
import com.fx.api.domain.User
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated

@Validated
interface UserCommandUseCase{

    fun signUp(@Valid signUpCommand: UserSignUpCommand): User

    /** 새 로그인 세션을 만들고 액세스 토큰과 리프레시 토큰을 발급한다. */
    fun login(@Valid loginCommand: UserLoginCommand): TokenInfo

    /**
     * 리프레시 토큰을 새것으로 바꾸고 액세스 토큰을 다시 발급한다.
     *
     * @throws com.fx.api.exception.AuthException 없거나 만료 · 이미 재발급에 쓴 리프레시 토큰 (INVALID_REFRESH_TOKEN)
     */
    fun refresh(refreshToken: String): TokenInfo

    /** 로그인 세션을 끝낸다. 이미 없는 세션이면 아무것도 하지 않는다. */
    fun logout(refreshToken: String)

}
