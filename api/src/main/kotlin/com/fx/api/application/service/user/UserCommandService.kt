package com.fx.api.application.service.user

import com.fx.api.application.port.`in`.user.UserCommandUseCase
import com.fx.api.application.port.`in`.user.dto.UserLoginCommand
import com.fx.api.application.port.`in`.user.dto.UserSignUpCommand
import com.fx.api.application.port.out.user.JwtProviderPort
import com.fx.api.application.port.out.user.PasswordEncoderPort
import com.fx.api.application.port.out.user.RefreshTokenPersistencePort
import com.fx.api.application.port.out.user.UserPersistencePort
import com.fx.api.config.security.AuthProperties
import com.fx.api.domain.RefreshToken
import com.fx.api.domain.TokenInfo
import com.fx.api.domain.User
import com.fx.api.exception.AuthException
import com.fx.api.exception.UserException
import com.fx.api.exception.errorcode.AuthErrorCode
import com.fx.api.exception.errorcode.UserErrorCode
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class UserCommandService(
    private val userPersistencePort: UserPersistencePort,
    private val refreshTokenPersistencePort: RefreshTokenPersistencePort,
    private val jwtProviderPort: JwtProviderPort,
    private val passwordEncoderPort: PasswordEncoderPort,
    private val authProperties: AuthProperties,
    private val clock: Clock,
) : UserCommandUseCase {

    private val log = LoggerFactory.getLogger(UserCommandService::class.java)

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

    /** 만료된 세션은 따로 정리하는 작업 없이 로그인할 때 지운다. */
    @Transactional
    override fun login(loginCommand: UserLoginCommand): TokenInfo {
        val user = userPersistencePort.findByEmail(loginCommand.email)
            ?: throw UserException(UserErrorCode.USER_NOT_FOUND)

        if (!passwordEncoderPort.matches(loginCommand.password, user.password)) {
            throw UserException(UserErrorCode.INVALID_PASSWORD)
        }

        val now = LocalDateTime.now(clock)
        refreshTokenPersistencePort.deleteExpired(now)

        val refreshToken = RefreshToken.generateToken()
        refreshTokenPersistencePort.save(
            RefreshToken.issue(requireNotNull(user.id), refreshToken, now, authProperties.refreshTokenTtl)
        )
        return TokenInfo(
            accessToken = generateAccessToken(user),
            refreshToken = refreshToken,
        )
    }

    /**
     * 권한은 토큰이 아니라 DB 에서 다시 읽는다. 권한을 회수하면 다음 재발급부터 반영된다.
     * 거절할 때 지운 세션이 롤백되지 않도록 [AuthException] 은 롤백하지 않는다.
     */
    @Transactional(noRollbackFor = [AuthException::class])
    override fun refresh(refreshToken: String): TokenInfo {
        val now = LocalDateTime.now(clock)
        val tokenHash = RefreshToken.hash(refreshToken)

        val session = refreshTokenPersistencePort.findByTokenHashForUpdate(tokenHash)
            ?: run {
                revokeIfReused(tokenHash)
                throw AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN)
            }
        if (session.isExpired(now)) {
            refreshTokenPersistencePort.delete(session)
            throw AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN)
        }
        val user = userPersistencePort.findById(session.userId)
            ?: run {
                refreshTokenPersistencePort.delete(session)
                throw AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN)
            }

        val newRefreshToken = RefreshToken.generateToken()
        session.rotate(newRefreshToken, now, authProperties.refreshTokenTtl)
        return TokenInfo(
            accessToken = generateAccessToken(user),
            refreshToken = newRefreshToken,
        )
    }

    @Transactional
    override fun logout(refreshToken: String) {
        refreshTokenPersistencePort.deleteByTokenHash(RefreshToken.hash(refreshToken))
    }

    /**
     * 이미 새것으로 바뀐 토큰이 다시 들어오면 세션을 끊는다.
     * 정상 사용자와 토큰을 훔친 쪽 중 누가 먼저 재발급했는지 알 수 없으므로 둘 다 다시 로그인하게 한다.
     */
    private fun revokeIfReused(tokenHash: String) {
        val session = refreshTokenPersistencePort.findByPreviousTokenHash(tokenHash)
            ?: return
        log.warn("이미 재발급에 쓴 리프레시 토큰이 다시 들어와 세션을 끊습니다 - userId: {}", session.userId)
        refreshTokenPersistencePort.delete(session)
    }

    /** JWT 의 `userId` 클레임은 TSID 를 문자열로 담는다. */
    private fun generateAccessToken(user: User): String =
        jwtProviderPort.generateAccessToken(requireNotNull(user.id).toString(), user.role)

}
