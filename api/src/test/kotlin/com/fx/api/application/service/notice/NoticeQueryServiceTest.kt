package com.fx.api.application.service.notice

import com.fx.api.application.port.`in`.notice.dto.NoticeSearchCommand
import com.fx.api.application.port.out.notice.NoticePersistencePort
import com.fx.api.application.service.topic.TopicResolver
import com.fx.api.domain.NoticeQuery
import com.fx.api.fixture.TopicFixture
import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.domain.notice.Notice
import com.fx.common.domain.notice.NoticeContent
import com.fx.common.exception.NoticeException
import com.fx.common.exception.TopicException
import com.fx.common.exception.errorcode.NoticeErrorCode
import com.fx.persistence.withId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate

class NoticeQueryServiceTest : BehaviorSpec({

    val noticePersistencePort = mockk<NoticePersistencePort>()
    val catalogQueryUseCase = mockk<CatalogQueryUseCase>()
    every {
        catalogQueryUseCase.getTopicCatalog()
    } returns TopicFixture.CATALOG
    val noticeQueryService = NoticeQueryService(noticePersistencePort, TopicResolver(catalogQueryUseCase))

    fun notice(nttId: Long) =
        Notice.crawled(
            nttId = nttId,
            topic = TopicFixture.GENERAL_NEWS,
            title = "공지 $nttId",
            department = "학사팀",
            contentUrl = "https://www.ut.ac.kr/notice/$nttId",
            contentImageUrl = null,
            registrationDate = LocalDate.of(2026, 9, 30),
            isAttachment = false,
        ).withId(nttId * 10)

    Given("공지 목록 조회") {

        When("v2 토픽 코드로 조회하면") {
            clearMocks(noticePersistencePort)
            every {
                noticePersistencePort.findNotices(any())
            } returns listOf(notice(2), notice(1))

            Then("토픽 코드 · 커서 · 크기로 조회한다") {
                noticeQueryService.getNotices(NoticeSearchCommand(nttId = 100, topicId = 1, keyword = " ", size = 20)) shouldHaveSize 2
                verify(exactly = 1) {
                    noticePersistencePort.findNotices(NoticeQuery(nttId = 100, topicCode = 1, keyword = null, size = 20))
                }
            }
        }

        When("v1 토픽 이름으로 조회하면") {
            clearMocks(noticePersistencePort)
            every {
                noticePersistencePort.findNotices(any())
            } returns listOf(notice(1))

            Then("이름을 토픽 코드로 바꿔 조회한다") {
                noticeQueryService.getNotices(NoticeSearchCommand(topicName = "COMPUTER_SOFTWARE", keyword = "장학", size = 10))
                verify(exactly = 1) {
                    noticePersistencePort.findNotices(NoticeQuery(nttId = null, topicCode = 300, keyword = "장학", size = 10))
                }
            }
        }

        When("조회 결과가 비어 있으면") {
            clearMocks(noticePersistencePort)
            every {
                noticePersistencePort.findNotices(any())
            } returns emptyList()

            Then("NOTICE_NOT_FOUND 예외가 발생한다") {
                val exception = shouldThrow<NoticeException> {
                    noticeQueryService.getNotices(NoticeSearchCommand(size = 20))
                }
                exception.baseErrorCode shouldBe NoticeErrorCode.NOTICE_NOT_FOUND
            }
        }

        When("존재하지 않는 토픽으로 조회하면") {
            Then("TopicException 이 발생한다") {
                shouldThrow<TopicException> {
                    noticeQueryService.getNotices(NoticeSearchCommand(topicName = "UNKNOWN", size = 20))
                }
            }
        }
    }

    Given("공지 요약 조회") {
        val target = notice(7)

        When("요약이 있으면") {
            clearMocks(noticePersistencePort)
            every {
                noticePersistencePort.getNotice(7)
            } returns target
            every {
                noticePersistencePort.findNoticeContent(70)
            } returns NoticeContent(70, content = "본문", contentSummary = "요약")

            Then("요약을 반환한다") {
                noticeQueryService.getNoticeSummary(7) shouldBe "요약"
            }
        }

        When("요약이 없으면") {
            clearMocks(noticePersistencePort)
            every {
                noticePersistencePort.getNotice(7)
            } returns target
            every {
                noticePersistencePort.findNoticeContent(70)
            } returns NoticeContent(70, content = "본문", contentSummary = null)

            Then("SUMMARY_CONTENT_NOT_FOUND 예외가 발생한다") {
                val exception = shouldThrow<NoticeException> {
                    noticeQueryService.getNoticeSummary(7)
                }
                exception.baseErrorCode shouldBe NoticeErrorCode.SUMMARY_CONTENT_NOT_FOUND
            }
        }
    }

})
