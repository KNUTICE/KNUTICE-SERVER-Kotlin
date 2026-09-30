package com.fx.api.adapter.`in`.web.user

import com.fx.api.adapter.`in`.web.user.dto.TokenResponse
import com.fx.api.adapter.`in`.web.user.dto.UserIdResponse
import com.fx.api.adapter.`in`.web.user.dto.UserLoginRequest
import com.fx.api.adapter.`in`.web.user.dto.UserSignUpRequest
import com.fx.api.application.port.`in`.user.UserCommandUseCase
import com.fx.common.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/open-api/v1/users")
class UserOpenApiAdapter(
    private val userCommandUseCase: UserCommandUseCase
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
        Api.OK(
            TokenResponse.from(
                userCommandUseCase.login(loginRequest.toCommand()),
            ),
            "로그인되었습니다."
        )

}