package com.fx.api.application.service.user

import com.fx.api.application.port.`in`.user.UserCommandUseCase
import com.fx.api.application.port.`in`.user.dto.UserLoginCommand
import com.fx.api.application.port.`in`.user.dto.UserSignUpCommand
import com.fx.api.application.port.out.user.JwtProviderPort
import com.fx.api.application.port.out.user.PasswordEncoderPort
import com.fx.api.application.port.out.user.UserPersistencePort
import com.fx.api.domain.TokenInfo
import com.fx.api.domain.User
import com.fx.api.exception.UserException
import com.fx.api.exception.errorcode.UserErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserCommandService(
    private val userPersistencePort: UserPersistencePort,
    private val jwtProviderPort: JwtProviderPort,
    private val passwordEncoderPort: PasswordEncoderPort,
) : UserCommandUseCase {

    @Transactional
    override fun signUp(signUpCommand: UserSignUpCommand): User {
        if (userPersistencePort.existsByEmail(signUpCommand.email)) {
            throw UserException(UserErrorCode.EMAIL_EXISTS)
        }
        if (userPersistencePort.existsByNickname(signUpCommand.nickname)) {
            throw UserException(UserErrorCode.NICKNAME_EXISTS)
        }

        return userPersistencePort.save(
            User.signUp(
                email = signUpCommand.email,
                encodedPassword = passwordEncoderPort.encode(signUpCommand.password),
                nickname = signUpCommand.nickname,
            )
        )
    }

    /** JWT 의 `userId` 클레임은 TSID 를 문자열로 담는다. */
    override fun login(loginCommand: UserLoginCommand): TokenInfo {
        val user = userPersistencePort.findByEmail(loginCommand.email)
            ?: throw UserException(UserErrorCode.USER_NOT_FOUND)

        if (!passwordEncoderPort.matches(loginCommand.password, user.password)) {
            throw UserException(UserErrorCode.INVALID_PASSWORD)
        }
        return jwtProviderPort.generateTokens(requireNotNull(user.id).toString(), user.role)
    }

}
