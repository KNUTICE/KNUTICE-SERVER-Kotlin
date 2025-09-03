package com.fx.api.application.service

import com.fx.api.application.port.`in`.UserCommandUseCase
import com.fx.api.application.port.`in`.dto.UserLoginCommand
import com.fx.api.application.port.`in`.dto.UserSignUpCommand
import com.fx.api.application.port.out.JwtProviderPort
import com.fx.api.application.port.out.PasswordEncoderPort
import com.fx.api.application.port.out.UserPersistencePort
import com.fx.api.domain.TokenInfo
import com.fx.api.domain.User
import com.fx.api.exception.UserException
import com.fx.api.exception.errorcode.UserErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserCommandService(
    private val userPersistencePort: UserPersistencePort,
    private val jwtProviderPort: JwtProviderPort,
    private val passwordEncoderPort: PasswordEncoderPort,
) : UserCommandUseCase {

    @Transactional
    override fun signUp(signUpCommand: UserSignUpCommand): User {

        // email, nickname 존재시 예외
        if (userPersistencePort.existsByEmail(signUpCommand.email)) {
            throw UserException(UserErrorCode.EMAIL_EXISTS)
        }

        if (userPersistencePort.existsByNickname(signUpCommand.nickname)) {
            throw UserException(UserErrorCode.NICKNAME_EXISTS)
        }

        // 암호화
        signUpCommand.password = passwordEncoderPort.encode(signUpCommand.password)

        // 등록
        return userPersistencePort.save(User.createUser(signUpCommand))
    }

    @Transactional
    override fun login(loginCommand: UserLoginCommand): TokenInfo {

        val user = userPersistencePort.findByEmail(loginCommand.email)
            ?: throw UserException(UserErrorCode.USER_NOT_FOUND)

        if (!passwordEncoderPort.matches(loginCommand.password, user.password)) {
            throw UserException(UserErrorCode.INVALID_PASSWORD)
        }


        return jwtProviderPort.generateTokens(user.id!!, user.role)
    }

}