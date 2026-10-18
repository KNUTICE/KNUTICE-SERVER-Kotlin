package com.fx.api.application.service.user

import com.fx.api.application.port.`in`.user.dto.UserLoginCommand
import com.fx.api.application.port.out.user.JwtProviderPort
import com.fx.api.application.port.out.user.PasswordEncoderPort
import com.fx.api.application.port.out.user.RefreshTokenPersistencePort
import com.fx.api.application.port.out.user.UserPersistencePort
import com.fx.api.config.security.AuthProperties
import com.fx.api.domain.RefreshToken
import com.fx.api.domain.User
import com.fx.api.domain.UserRole
import com.fx.api.exception.AuthException
import com.fx.api.exception.UserException
import com.fx.api.exception.errorcode.AuthErrorCode
import com.fx.api.exception.errorcode.UserErrorCode
import com.fx.persistence.withId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class UserCommandServiceTest : BehaviorSpec({

    val userPersistencePort = mockk<UserPersistencePort>()
    val refreshTokenPersistencePort = mockk<RefreshTokenPersistencePort>(relaxUnitFun = true)
    val jwtProviderPort = mockk<JwtProviderPort>()
    val passwordEncoderPort = mockk<PasswordEncoderPort>()
    val properties = AuthProperties(
        jwtSecretKey = "unused",
        accessTokenTtl = Duration.ofHours(1),
        refreshTokenTtl = Duration.ofDays(14),
    )
    val clock = Clock.fixed(Instant.parse("2026-10-15T00:00:00Z"), ZoneId.of("Asia/Seoul"))
    val now = LocalDateTime.now(clock)
    val service = UserCommandService(
        userPersistencePort, refreshTokenPersistencePort, jwtProviderPort, passwordEncoderPort, properties, clock,
    )

    val admin = User("admin@knutice.com", "encoded", "관리자", UserRole.ADMIN).withId(10L)

    fun reset() {
        clearMocks(userPersistencePort, refreshTokenPersistencePort, jwtProviderPort, passwordEncoderPort)
        every {
            jwtProviderPort.generateAccessToken("10", UserRole.ADMIN)
        } returns "access-token"
    }

    fun shouldFailWith(errorCode: AuthErrorCode, block: () -> Unit) {
        shouldThrow<AuthException> {
            block()
        }.baseErrorCode shouldBe errorCode
    }

    Given("로그인") {
        val command = UserLoginCommand(email = "admin@knutice.com", password = "password1")

        When("비밀번호가 맞으면") {
            reset()
            every {
                userPersistencePort.findByEmail("admin@knutice.com")
            } returns admin
            every {
                passwordEncoderPort.matches("password1", "encoded")
            } returns true
            val saved = slot<RefreshToken>()
            every {
                refreshTokenPersistencePort.save(capture(saved))
            } answers {
                saved.captured
            }

            val tokens = service.login(command)

            Then("액세스 토큰과 새 세션의 리프레시 토큰을 발급한다") {
                tokens.accessToken shouldBe "access-token"
                saved.captured.userId shouldBe 10L
                saved.captured.tokenHash shouldBe RefreshToken.hash(tokens.refreshToken)
                saved.captured.expiresAt shouldBe now.plusDays(14)
            }

            Then("만료된 세션을 지운다") {
                verify {
                    refreshTokenPersistencePort.deleteExpired(now)
                }
            }
        }

        When("비밀번호가 틀리면") {
            reset()
            every {
                userPersistencePort.findByEmail("admin@knutice.com")
            } returns admin
            every {
                passwordEncoderPort.matches("password1", "encoded")
            } returns false

            Then("세션을 만들지 않는다") {
                shouldThrow<UserException> {
                    service.login(command)
                }.baseErrorCode shouldBe UserErrorCode.INVALID_PASSWORD
                verify(exactly = 0) {
                    refreshTokenPersistencePort.save(any())
                }
            }
        }
    }

    Given("토큰 재발급") {

        When("현재 리프레시 토큰이면") {
            reset()
            val session = RefreshToken.issue(10L, "current", now.minusDays(13), properties.refreshTokenTtl)
            every {
                refreshTokenPersistencePort.findByTokenHashForUpdate(RefreshToken.hash("current"))
            } returns session
            every {
                userPersistencePort.findById(10L)
            } returns admin

            val tokens = service.refresh("current")

            Then("리프레시 토큰을 새것으로 바꾸고 만료를 재발급 시각부터 다시 센다") {
                tokens.accessToken shouldBe "access-token"
                session.tokenHash shouldBe RefreshToken.hash(tokens.refreshToken)
                session.previousTokenHash shouldBe RefreshToken.hash("current")
                session.expiresAt shouldBe now.plusDays(14)
            }
        }

        When("이미 재발급에 쓴 직전 토큰이 다시 들어오면") {
            reset()
            val session = RefreshToken.issue(10L, "previous", now.minusDays(1), properties.refreshTokenTtl)
            session.rotate("current", now.minusHours(1), properties.refreshTokenTtl)
            every {
                refreshTokenPersistencePort.findByTokenHashForUpdate(RefreshToken.hash("previous"))
            } returns null
            every {
                refreshTokenPersistencePort.findByPreviousTokenHash(RefreshToken.hash("previous"))
            } returns session

            Then("탈취로 보고 세션을 끊는다") {
                shouldFailWith(AuthErrorCode.INVALID_REFRESH_TOKEN) {
                    service.refresh("previous")
                }
                verify {
                    refreshTokenPersistencePort.delete(session)
                }
            }
        }

        When("어느 세션의 토큰도 아니면") {
            reset()
            every {
                refreshTokenPersistencePort.findByTokenHashForUpdate(any())
            } returns null
            every {
                refreshTokenPersistencePort.findByPreviousTokenHash(any())
            } returns null

            Then("거절만 한다") {
                shouldFailWith(AuthErrorCode.INVALID_REFRESH_TOKEN) {
                    service.refresh("unknown")
                }
                verify(exactly = 0) {
                    refreshTokenPersistencePort.delete(any())
                }
            }
        }

        When("세션이 만료됐으면") {
            reset()
            val session = RefreshToken.issue(10L, "current", now.minusDays(14), properties.refreshTokenTtl)
            every {
                refreshTokenPersistencePort.findByTokenHashForUpdate(RefreshToken.hash("current"))
            } returns session

            Then("세션을 지우고 거절한다") {
                shouldFailWith(AuthErrorCode.INVALID_REFRESH_TOKEN) {
                    service.refresh("current")
                }
                verify {
                    refreshTokenPersistencePort.delete(session)
                }
            }
        }

        When("사용자가 없어졌으면") {
            reset()
            val session = RefreshToken.issue(10L, "current", now.minusDays(1), properties.refreshTokenTtl)
            every {
                refreshTokenPersistencePort.findByTokenHashForUpdate(RefreshToken.hash("current"))
            } returns session
            every {
                userPersistencePort.findById(10L)
            } returns null

            Then("세션을 지우고 거절한다") {
                shouldFailWith(AuthErrorCode.INVALID_REFRESH_TOKEN) {
                    service.refresh("current")
                }
                verify {
                    refreshTokenPersistencePort.delete(session)
                }
            }
        }
    }

    Given("로그아웃") {
        reset()
        service.logout("current")

        Then("그 토큰의 세션을 지운다") {
            verify {
                refreshTokenPersistencePort.deleteByTokenHash(RefreshToken.hash("current"))
            }
        }
    }

})
