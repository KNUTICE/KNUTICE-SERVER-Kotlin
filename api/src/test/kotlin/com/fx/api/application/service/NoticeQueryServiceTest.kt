package com.fx.api.application.service

import com.fx.api.application.port.out.NoticePersistencePort
import com.fx.api.domain.NoticeQuery
import com.fx.api.exception.NoticeException
import com.fx.api.exception.errorcode.NoticeErrorCode
import com.fx.global.domain.CrawlableType
import com.fx.global.domain.Notice
import com.fx.global.domain.NoticeType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.LocalDate

class NoticeQueryServiceTest : BehaviorSpec({

    val noticePersistencePort = mockk<NoticePersistencePort>(relaxed = true)
    val noticeQueryService = NoticeQueryService(noticePersistencePort)

    fun createNotice(size: Int, type: CrawlableType): List<Notice> =
        (1..size).map { i ->
            Notice(
                nttId = i.toLong(),
                title = "title$i",
                department = "department$i",
                contentUrl = "https://knutice/$i",
                contentImageUrl = "https://knutice-image/$i",
                registrationDate = LocalDate.now(),
                isAttachment = false,
                topic = type
            )
        }

    Given("공지 목록 조회") {
        val pageSize = 20
        val noticeQuery = NoticeQuery(
            topic = NoticeType.GENERAL_NEWS,
            pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "nttId"))
        )
        val notices = createNotice(pageSize, NoticeType.GENERAL_NEWS)

        When("GENERAL_NEWS 목록 조회") {
            every { noticePersistencePort.getNotices(noticeQuery) } returns notices

            Then("목록 정상 반환") {
                val result = noticeQueryService.getNotices(noticeQuery)
                result.size shouldBe pageSize
                result.first().title shouldBe "title1"
            }
        }

        When("공지 목록이 비어있는 경우") {
            every { noticePersistencePort.getNotices(noticeQuery) } returns emptyList()

            Then("NoticeException 예외 발생") {
                val exception = shouldThrow<NoticeException> { noticeQueryService.getNotices(noticeQuery) }
                exception.baseErrorCode shouldBe  NoticeErrorCode.NOTICE_NOT_FOUND
            }
        }
    }

    Given("단건 공지 조회") {
        val notice = createNotice(1, NoticeType.GENERAL_NEWS).first()

        When("존재하는 nttId 조회") {
            every { noticePersistencePort.getNotice(notice.nttId) } returns notice

            Then("정상 반환") {
                val result = noticeQueryService.getNotice(notice.nttId)
                result.title shouldBe notice.title
            }
        }

        When("존재하지 않는 nttId 조회") {
            every { noticePersistencePort.getNotice(999L) } returns null

            Then("NoticeException 예외 발생") {
                val exception = shouldThrow<NoticeException> { noticeQueryService.getNotice(999L) }
                exception.baseErrorCode shouldBe  NoticeErrorCode.NOTICE_NOT_FOUND
            }
        }
    }
})