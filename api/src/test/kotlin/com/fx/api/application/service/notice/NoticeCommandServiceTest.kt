package com.fx.api.application.service.notice

import com.fx.api.application.port.`in`.notice.dto.NoticeCommand
import com.fx.api.application.port.out.notice.NoticePersistencePort
import com.fx.api.application.service.topic.TopicResolver
import com.fx.api.fixture.TopicFixture
import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.domain.TopicType
import com.fx.common.domain.notice.Notice
import com.fx.common.domain.notice.NoticeContent
import com.fx.common.domain.notice.NotificationStatus
import com.fx.common.domain.notice.SummaryStatus
import com.fx.common.exception.NoticeException
import com.fx.common.exception.TopicException
import com.fx.common.exception.errorcode.NoticeErrorCode
import com.fx.persistence.withId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDate

class NoticeCommandServiceTest : BehaviorSpec({

    val noticePersistencePort = mockk<NoticePersistencePort>(relaxed = true)
    val catalogQueryUseCase = mockk<CatalogQueryUseCase>()
    every {
        catalogQueryUseCase.getTopicCatalog()
    } returns TopicFixture.CATALOG
    val noticeCommandService = NoticeCommandService(noticePersistencePort, TopicResolver(catalogQueryUseCase))

    fun command(summary: String?, topicName: String = "GENERAL_NEWS", topicType: TopicType = TopicType.NOTICE) =
        NoticeCommand(
            nttId = 1L,
            title = "관리자 공지",
            contentUrl = "https://www.ut.ac.kr/notice/1",
            contentSummary = summary,
            department = "학사팀",
            registrationDate = LocalDate.of(2026, 9, 30),
            isAttachment = false,
            topicName = topicName,
            topicType = topicType,
        )

    Given("관리자 공지 등록") {

        When("요약과 함께 등록하면") {
            clearMocks(noticePersistencePort)
            every {
                noticePersistencePort.existsByNttId(1L)
            } returns false
            val saved = slot<Notice>()
            every {
                noticePersistencePort.create(capture(saved), any())
            } answers {
                saved.captured
            }

            Then("알림은 보내지 않고 요약은 있는 상태로 저장한다") {
                noticeCommandService.saveNotice(command(summary = "요약")) shouldBe true
                saved.captured.notificationStatus shouldBe NotificationStatus.SKIPPED
                saved.captured.summaryStatus shouldBe SummaryStatus.COMPLETED
                saved.captured.topicCode shouldBe 1
                verify(exactly = 1) {
                    noticePersistencePort.create(any(), "요약")
                }
            }
        }

        When("이미 있는 nttId 면") {
            clearMocks(noticePersistencePort)
            every {
                noticePersistencePort.existsByNttId(1L)
            } returns true

            Then("ALREADY_EXISTS 예외가 발생한다") {
                val exception = shouldThrow<NoticeException> {
                    noticeCommandService.saveNotice(command(summary = null))
                }
                exception.baseErrorCode shouldBe NoticeErrorCode.ALREADY_EXISTS
            }
        }

        When("토픽이 요청한 유형에 속하지 않으면") {
            clearMocks(noticePersistencePort)
            every {
                noticePersistencePort.existsByNttId(1L)
            } returns false

            Then("TopicException 이 발생한다") {
                shouldThrow<TopicException> {
                    noticeCommandService.saveNotice(command(summary = null, topicName = "GENERAL_NEWS", topicType = TopicType.MAJOR))
                }
            }
        }
    }

    Given("관리자 공지 수정") {

        When("요약을 지우면") {
            clearMocks(noticePersistencePort)
            val notice = Notice.registeredByAdmin(
                nttId = 1L, topic = TopicFixture.GENERAL_NEWS, title = "이전", department = "학사팀",
                contentUrl = "https://www.ut.ac.kr/notice/1", contentImageUrl = null,
                registrationDate = LocalDate.of(2026, 9, 1), isAttachment = false, hasSummary = true,
            ).withId(10L)
            val content = NoticeContent(10L, content = "본문", contentSummary = "요약")
            every {
                noticePersistencePort.getNotice(1L)
            } returns notice
            every {
                noticePersistencePort.findNoticeContent(10L)
            } returns content

            Then("필드를 바꾸고 요약 상태를 SKIPPED 로 바꾼다. 본문은 그대로 둔다") {
                noticeCommandService.updateNotice(command(summary = null, topicName = "SCHOLARSHIP_NEWS")) shouldBe true
                notice.title shouldBe "관리자 공지"
                notice.topicName shouldBe "SCHOLARSHIP_NEWS"
                notice.summaryStatus shouldBe SummaryStatus.SKIPPED
                content.contentSummary shouldBe null
                content.content shouldBe "본문"
            }
        }
    }

})
