package com.fx.api.application.port.`in`

import com.fx.api.application.port.`in`.dto.UserLoginCommand
import com.fx.api.application.port.`in`.dto.UserSignUpCommand
import com.fx.api.domain.TokenInfo
import com.fx.api.domain.User
import jakarta.validation.Valid
import org.springframework.validation.annotation.Validated

@Validated
interface UserCommandUseCase{

    suspend fun signUp(@Valid signUpCommand: UserSignUpCommand): User

    suspend fun login(@Valid loginCommand: UserLoginCommand): TokenInfo

}
