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

    fun login(@Valid loginCommand: UserLoginCommand): TokenInfo

}
