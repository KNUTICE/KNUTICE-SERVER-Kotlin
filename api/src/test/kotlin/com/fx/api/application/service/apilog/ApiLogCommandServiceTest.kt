package com.fx.api.application.service.apilog

import com.fx.api.application.port.`in`.apilog.dto.ApiLogCommand
import com.fx.api.application.port.out.apilog.ApiLogPersistencePort
import com.fx.api.application.port.out.fcmtoken.FcmTokenPersistencePort
import com.fx.api.domain.ApiLog
import com.fx.api.fixture.TopicFixture
import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.domain.fcmtoken.FCM_TOKEN_MAX_LENGTH
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDateTime

class ApiLogCommandServiceTest : BehaviorSpec({

    val apiLogPersistencePort = mockk<ApiLogPersistencePort>()
    val fcmTokenPersistencePort = mockk<FcmTokenPersistencePort>()
    val catalogQueryUseCase = mockk<CatalogQueryUseCase>()
    val apiLogCommandService = ApiLogCommandService(apiLogPersistencePort, fcmTokenPersistencePort, catalogQueryUseCase)

    val requestedAt = LocalDateTime.of(2026, 10, 13, 23, 59, 59)
    val tooLongToken = "t".repeat(FCM_TOKEN_MAX_LENGTH + 1)

    fun command(
        urlPattern: String,
        topicName: String? = null,
        topicId: String? = null,
        fcmToken: String? = null,
    ) =
        ApiLogCommand(
            requestedAt = requestedAt,
            method = "GET",
            urlPattern = urlPattern,
            statusCode = 200,
            durationMs = 15,
            topicName = topicName,
            topicId = topicId,
            fcmToken = fcmToken,
        )

    Given("요청 기록 묶음 저장") {

        When("토큰 · 토픽 파라미터가 섞여 있으면") {
            clearMocks(apiLogPersistencePort, fcmTokenPersistencePort, catalogQueryUseCase)
            every {
                catalogQueryUseCase.getTopicCatalog()
            } returns TopicFixture.CATALOG
            every {
                fcmTokenPersistencePort.findIdsByTokens(any())
            } returns mapOf("token-a" to 10L)
            val saved = slot<List<ApiLog>>()
            every {
                apiLogPersistencePort.saveAll(capture(saved))
            } returns Unit

            apiLogCommandService.saveAll(
                listOf(
                    command("/open-api/v1/notices", topicName = "GENERAL_NEWS", fcmToken = "token-a"),
                    command("/open-api/v2/notices", topicId = "300", fcmToken = "token-a"),
                    command("/open-api/v1/notices", topicName = "UNKNOWN_TOPIC", fcmToken = "token-unknown"),
                    command("/open-api/v2/notices", topicId = "999"),
                    command("/open-api/v2/notices", topicId = "abc"),
                    command("/open-api/v1/topics", fcmToken = tooLongToken),
                )
            )
            val logs = saved.captured

            Then("요청 값을 그대로 옮긴다") {
                logs.size shouldBe 6
                logs.first().requestedAt shouldBe requestedAt
                logs.first().method shouldBe "GET"
                logs.first().urlPattern shouldBe "/open-api/v1/notices"
                logs.first().statusCode shouldBe 200
                logs.first().durationMs shouldBe 15
            }

            Then("토큰은 묶음당 한 번, 중복 없이 조회해 ID 로 바꾼다. 512자를 넘는 토큰은 조회하지 않는다") {
                verify(exactly = 1) {
                    fcmTokenPersistencePort.findIdsByTokens(setOf("token-a", "token-unknown"))
                }
                logs.map {
                    it.fcmTokenId
                } shouldBe listOf(10L, 10L, null, null, null, null)
            }

            Then("v1 토픽 이름 · v2 토픽 코드를 토픽 코드 · 이름으로 바꾼다. 모르는 토픽은 비워 둔다") {
                logs.map {
                    it.topicCode to it.topicName
                } shouldBe listOf(
                    1 to "GENERAL_NEWS",
                    300 to "COMPUTER_SOFTWARE",
                    null to null,
                    null to null,
                    null to null,
                    null to null,
                )
            }
        }

        When("묶음이 비어 있으면") {
            clearMocks(apiLogPersistencePort, fcmTokenPersistencePort, catalogQueryUseCase)

            apiLogCommandService.saveAll(emptyList())

            Then("아무것도 조회 · 저장하지 않는다") {
                verify(exactly = 0) {
                    fcmTokenPersistencePort.findIdsByTokens(any())
                    catalogQueryUseCase.getTopicCatalog()
                    apiLogPersistencePort.saveAll(any())
                }
            }
        }
    }

})
