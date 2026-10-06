package com.fx.api.application.service.batch

import com.fx.api.application.port.`in`.batch.dto.BatchScheduleCreateCommand
import com.fx.api.application.port.`in`.batch.dto.BatchScheduleUpdateCommand
import com.fx.api.application.port.out.batch.BatchExecutionPersistencePort
import com.fx.api.application.port.out.batch.BatchSchedulePersistencePort
import com.fx.api.domain.LastJobExecution
import com.fx.api.exception.BatchException
import com.fx.api.exception.errorcode.BatchErrorCode
import com.fx.common.domain.batch.BatchSchedule
import com.fx.persistence.withId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

class BatchScheduleCommandQueryServiceTest : BehaviorSpec({

    val zone = ZoneId.of("Asia/Seoul")
    val now = LocalDateTime.of(2026, 10, 6, 14, 7)
    val clock = Clock.fixed(now.atZone(zone).toInstant(), zone)

    val schedulePort = mockk<BatchSchedulePersistencePort>()
    val executionPort = mockk<BatchExecutionPersistencePort>()
    val service = BatchScheduleCommandQueryService(schedulePort, executionPort, clock)

    fun resetMocks() {
        clearMocks(schedulePort, executionPort)
        every {
            executionPort.findLastExecutions(any())
        } returns emptyMap()
        every {
            schedulePort.existsByScheduleKey(any())
        } returns false
        every {
            schedulePort.create(any())
        } answers {
            firstArg()
        }
    }

    fun createCommand(
        jobName: String = "noticeCrawlJob",
        jobParameters: Map<String, String> = mapOf("topicType" to "NOTICE"),
        cron: String = "0 0 23 * * *",
        enabled: Boolean = true,
    ) =
        BatchScheduleCreateCommand(
            scheduleKey = "notice-crawl-notice-night",
            jobName = jobName,
            jobParameters = jobParameters,
            cron = cron,
            enabled = enabled,
            description = "공지 게시판 야간 크롤링",
        )

    /** 매일 16:10 에 실행하는 학과 크롤링 스케줄 */
    fun majorSchedule(enabled: Boolean = true) =
        BatchSchedule("notice-crawl-major", "noticeCrawlJob", """{"topicType":"MAJOR"}""", "0 10 16 * * *", false, "학과 게시판 크롤링")
            .withId(1L)
            .apply {
                if (enabled) {
                    enable(now)
                }
            }

    Given("스케줄 목록") {

        When("조회하면") {
            resetMocks()
            val major = majorSchedule()
            val silentPush = BatchSchedule("silent-push", "silentPushJob", "{}", "0 0 0 1 * *", true, "사일런트 푸시").withId(2L)
            every {
                schedulePort.findAll()
            } returns listOf(major, silentPush)
            every {
                executionPort.findLastExecutions(listOf("notice-crawl-major", "silent-push"))
            } returns mapOf("notice-crawl-major" to LastJobExecution(48231L, "COMPLETED"))

            Then("스케줄마다 마지막 실행을 붙이고, 실행 기록이 없으면 비워 둔다") {
                val schedules = service.getSchedules()

                schedules.map {
                    it.schedule.scheduleKey
                } shouldBe listOf("notice-crawl-major", "silent-push")
                schedules[0].lastExecution shouldBe LastJobExecution(48231L, "COMPLETED")
                schedules[1].lastExecution shouldBe null
            }
        }
    }

    Given("스케줄 추가") {

        When("켠 채로 추가하면") {
            resetMocks()

            Then("다음 실행 시각을 바로 계산하고 파라미터는 JSON 으로 저장한다") {
                val schedule = service.createSchedule(createCommand()).schedule

                schedule.enabled shouldBe true
                schedule.nextFireAt shouldBe LocalDateTime.of(2026, 10, 6, 23, 0)
                schedule.jobParameters shouldBe """{"topicType":"NOTICE"}"""
            }
        }

        When("끈 채로 추가하면") {
            resetMocks()

            Then("다음 실행 시각이 없다") {
                val schedule = service.createSchedule(createCommand(enabled = false)).schedule

                schedule.enabled shouldBe false
                schedule.nextFireAt shouldBe null
            }
        }

        When("이미 있는 스케줄 키면") {
            resetMocks()
            every {
                schedulePort.existsByScheduleKey("notice-crawl-notice-night")
            } returns true

            Then("SCHEDULE_KEY_DUPLICATED 예외가 발생하고 저장하지 않는다") {
                shouldThrow<BatchException> {
                    service.createSchedule(createCommand())
                }.baseErrorCode shouldBe BatchErrorCode.SCHEDULE_KEY_DUPLICATED
                verify(exactly = 0) {
                    schedulePort.create(any())
                }
            }
        }

        When("등록되지 않은 Job 이면") {
            resetMocks()

            Then("JOB_NOT_FOUND 예외가 발생한다") {
                val e = shouldThrow<BatchException> {
                    service.createSchedule(createCommand(jobName = "unknownJob"))
                }
                e.baseErrorCode shouldBe BatchErrorCode.JOB_NOT_FOUND
                e.message shouldContain "unknownJob"
            }
        }

        When("Job 의 파라미터 규칙에 맞지 않으면") {
            resetMocks()

            Then("JOB_PARAMETERS_INVALID 예외에 이유를 담는다") {
                mapOf(
                    emptyMap<String, String>() to "noticeCrawlJob 에는 topicType 파라미터가 필요합니다.",
                    mapOf("topicType" to "MEAL") to "topicType 은 NOTICE / MAJOR 중 하나여야 합니다: MEAL",
                    mapOf("topicType" to "NOTICE", "scheduledAt" to "x") to "noticeCrawlJob 에는 없는 파라미터입니다: scheduledAt",
                ).forEach { (jobParameters, message) ->
                    val e = shouldThrow<BatchException> {
                        service.createSchedule(createCommand(jobParameters = jobParameters))
                    }
                    e.baseErrorCode shouldBe BatchErrorCode.JOB_PARAMETERS_INVALID
                    e.message shouldBe message
                }
            }
        }

        When("cron 이 올바르지 않으면") {
            resetMocks()

            Then("CRON_INVALID 예외가 발생한다") {
                listOf(
                    "*/10 * * * * *", // 분 단위보다 촘촘함
                    "0 0 23 * *", // 5자리
                    "0 0 25 * * *", // 없는 시각
                    "0 0 0 30 2 *", // 다음 실행 시각이 없음
                ).forEach { cron ->
                    shouldThrow<BatchException> {
                        service.createSchedule(createCommand(cron = cron))
                    }.baseErrorCode shouldBe BatchErrorCode.CRON_INVALID
                }
            }
        }
    }

    Given("스케줄 수정") {

        When("켜진 스케줄의 cron 을 바꾸면") {
            resetMocks()
            val schedule = majorSchedule()
            every {
                schedulePort.getByScheduleKey("notice-crawl-major")
            } returns schedule

            Then("다음 실행 시각을 새 cron 으로 다시 계산하고 보내지 않은 값은 그대로 둔다") {
                service.updateSchedule(BatchScheduleUpdateCommand("notice-crawl-major", " 0 0 15 * * * ", null, null))

                schedule.cron shouldBe "0 0 15 * * *"
                schedule.nextFireAt shouldBe LocalDateTime.of(2026, 10, 6, 15, 0)
                schedule.jobParameters shouldBe """{"topicType":"MAJOR"}"""
                schedule.description shouldBe "학과 게시판 크롤링"
            }
        }

        When("파라미터와 설명을 바꾸면") {
            resetMocks()
            val schedule = majorSchedule()
            every {
                schedulePort.getByScheduleKey("notice-crawl-major")
            } returns schedule

            Then("스케줄의 Job 규칙으로 검증해 바꾸고 cron 은 그대로 둔다") {
                service.updateSchedule(
                    BatchScheduleUpdateCommand("notice-crawl-major", null, mapOf("topicType" to "NOTICE"), "공지 크롤링")
                )

                schedule.jobParameters shouldBe """{"topicType":"NOTICE"}"""
                schedule.description shouldBe "공지 크롤링"
                schedule.cron shouldBe "0 10 16 * * *"
            }
        }

        When("스케줄의 Job 이 받지 않는 파라미터로 바꾸면") {
            resetMocks()
            every {
                schedulePort.getByScheduleKey("notice-crawl-major")
            } returns majorSchedule()

            Then("JOB_PARAMETERS_INVALID 예외가 발생한다") {
                shouldThrow<BatchException> {
                    service.updateSchedule(
                        BatchScheduleUpdateCommand("notice-crawl-major", null, mapOf("retentionDays" to "7"), null)
                    )
                }.baseErrorCode shouldBe BatchErrorCode.JOB_PARAMETERS_INVALID
            }
        }
    }

    Given("자동 실행 켜기 · 끄기") {

        When("꺼진 스케줄을 켜면") {
            resetMocks()
            val schedule = majorSchedule(enabled = false)
            every {
                schedulePort.getByScheduleKey("notice-crawl-major")
            } returns schedule

            Then("지금 이후 첫 시각을 다음 실행 시각으로 정한다") {
                service.changeEnabled("notice-crawl-major", true)

                schedule.enabled shouldBe true
                schedule.nextFireAt shouldBe LocalDateTime.of(2026, 10, 6, 16, 10)
            }
        }

        When("켜진 스케줄을 끄면") {
            resetMocks()
            val schedule = majorSchedule()
            every {
                schedulePort.getByScheduleKey("notice-crawl-major")
            } returns schedule

            Then("다음 실행 시각을 지운다") {
                service.changeEnabled("notice-crawl-major", false)

                schedule.enabled shouldBe false
                schedule.nextFireAt shouldBe null
            }
        }
    }

})
