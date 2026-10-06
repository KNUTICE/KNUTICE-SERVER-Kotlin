package com.fx.api.application.service.batch

import com.fx.api.application.port.`in`.batch.dto.BatchRunRequestCommand
import com.fx.api.application.port.`in`.batch.dto.BatchRunRequestSearchCommand
import com.fx.api.application.port.out.batch.BatchRunRequestPersistencePort
import com.fx.api.application.port.out.user.UserPersistencePort
import com.fx.api.domain.User
import com.fx.api.domain.UserRole
import com.fx.api.exception.BatchException
import com.fx.api.exception.errorcode.BatchErrorCode
import com.fx.common.domain.batch.BatchRunRequest
import com.fx.common.domain.batch.BatchRunRequestStatus
import com.fx.persistence.withId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class BatchRunRequestCommandQueryServiceTest : BehaviorSpec({

    val requestPort = mockk<BatchRunRequestPersistencePort>()
    val userPort = mockk<UserPersistencePort>()
    val service = BatchRunRequestCommandQueryService(requestPort, userPort)

    fun resetMocks() {
        clearMocks(requestPort, userPort)
        every {
            requestPort.findRequested(any())
        } returns emptyList()
        every {
            requestPort.save(any())
        } answers {
            firstArg()
        }
        every {
            userPort.getById(1L)
        } returns User("admin@knutice.test", "encoded", "관리자", UserRole.ADMIN).withId(1L)
    }

    fun command(jobName: String = "noticeCrawlJob", jobParameters: Map<String, String> = mapOf("topicType" to "NOTICE")) =
        BatchRunRequestCommand(jobName, jobParameters, userId = 1L)

    fun request(id: Long, jobParameters: String = """{"topicType":"NOTICE"}""") =
        BatchRunRequest("noticeCrawlJob", jobParameters, "관리자").withId(id)

    Given("수동 실행 요청") {

        When("올바른 Job · 파라미터로 요청하면") {
            resetMocks()

            Then("요청한 관리자의 닉네임으로 대기 상태로 저장한다") {
                val saved = service.requestRun(command())

                saved.jobName shouldBe "noticeCrawlJob"
                saved.jobParameters shouldBe """{"topicType":"NOTICE"}"""
                saved.requestedBy shouldBe "관리자"
                saved.status shouldBe BatchRunRequestStatus.REQUESTED
            }
        }

        When("같은 Job · 파라미터의 요청이 아직 대기 중이면") {
            resetMocks()
            every {
                requestPort.findRequested("noticeCrawlJob")
            } returns listOf(request(10L))

            Then("RUN_REQUEST_DUPLICATED 예외가 발생하고 저장하지 않는다") {
                shouldThrow<BatchException> {
                    service.requestRun(command())
                }.baseErrorCode shouldBe BatchErrorCode.RUN_REQUEST_DUPLICATED
                verify(exactly = 0) {
                    requestPort.save(any())
                }
            }
        }

        When("같은 Job 이라도 파라미터가 다른 요청만 대기 중이면") {
            resetMocks()
            every {
                requestPort.findRequested("noticeCrawlJob")
            } returns listOf(request(10L, """{"topicType":"MAJOR"}"""))

            Then("요청을 저장한다") {
                service.requestRun(command()).status shouldBe BatchRunRequestStatus.REQUESTED
            }
        }

        When("등록되지 않은 Job 이나 규칙에 맞지 않는 파라미터면") {
            resetMocks()

            Then("crawler 의 거절을 기다리지 않고 바로 예외가 발생한다") {
                shouldThrow<BatchException> {
                    service.requestRun(command(jobName = "unknownJob", jobParameters = emptyMap()))
                }.baseErrorCode shouldBe BatchErrorCode.JOB_NOT_FOUND
                shouldThrow<BatchException> {
                    service.requestRun(command(jobName = "maintenanceJob", jobParameters = emptyMap()))
                }.baseErrorCode shouldBe BatchErrorCode.JOB_PARAMETERS_INVALID
                verify(exactly = 0) {
                    requestPort.save(any())
                }
            }
        }
    }

    Given("수동 실행 요청 목록") {

        When("한 페이지보다 많이 남아 있으면") {
            resetMocks()
            every {
                requestPort.findRequests(null, null, 3)
            } returns listOf(request(30L), request(20L), request(10L))

            Then("요청한 크기만 돌려주고 마지막 요청 id 를 다음 커서로 준다") {
                val page = service.getRunRequests(BatchRunRequestSearchCommand(status = null, cursor = null, size = 2))

                page.items.map {
                    it.id
                } shouldBe listOf(30L, 20L)
                page.nextCursor shouldBe 20L
            }
        }

        When("마지막 페이지면") {
            resetMocks()
            every {
                requestPort.findRequests(BatchRunRequestStatus.REJECTED, 20L, 3)
            } returns listOf(request(10L))

            Then("다음 커서가 없다") {
                val page = service.getRunRequests(BatchRunRequestSearchCommand(BatchRunRequestStatus.REJECTED, cursor = 20L, size = 2))

                page.items.size shouldBe 1
                page.nextCursor shouldBe null
            }
        }
    }

})
