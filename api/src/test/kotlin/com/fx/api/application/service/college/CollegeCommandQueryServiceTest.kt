package com.fx.api.application.service.college

import com.fx.api.application.port.`in`.college.dto.CollegeDisplayNameUpdateCommand
import com.fx.api.application.port.out.college.CollegePersistencePort
import com.fx.api.exception.CollegeException
import com.fx.api.exception.errorcode.CollegeErrorCode
import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.domain.college.College
import com.fx.common.domain.i18n.LocalizedText
import com.fx.persistence.withId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder

class CollegeCommandQueryServiceTest : BehaviorSpec({

    val collegePort = mockk<CollegePersistencePort>()
    val catalogQueryUseCase = mockk<CatalogQueryUseCase>(relaxUnitFun = true)
    val service = CollegeCommandQueryService(collegePort, catalogQueryUseCase)

    fun college(id: Long, collegeKey: String, displayOrder: Int) =
        College(collegeKey, LocalizedText(collegeKey), displayOrder).withId(id)

    Given("단과대 목록을 조회하면") {
        every {
            collegePort.findAll()
        } returns listOf(college(1L, "ENGINEERING", 2), college(2L, "HUMANITIES", 5))
        every {
            collegePort.countTopicsByCollegeId()
        } returns mapOf(1L to 14L)

        When("소속 학과 수를 붙일 때") {
            val details = service.getColleges()

            Then("저장소가 준 순서를 지키고, 학과가 없는 단과대는 0 으로 둔다") {
                details.map {
                    it.college.collegeKey to it.topicCount
                } shouldBe listOf("ENGINEERING" to 14L, "HUMANITIES" to 0L)
            }
        }
    }

    Given("단과대 표시명을 수정하면") {
        val displayName = LocalizedText("공과대학", "College of Engineering", "工学部")
        val command = CollegeDisplayNameUpdateCommand("ENGINEERING", displayName)

        When("단과대가 있으면") {
            clearMocks(collegePort, catalogQueryUseCase)
            every {
                collegePort.updateDisplayName("ENGINEERING", displayName)
            } returns college(1L, "ENGINEERING", 2).apply {
                changeDisplayName(displayName)
            }
            every {
                collegePort.countTopicsByCollegeId()
            } returns mapOf(1L to 14L)

            val detail = service.updateDisplayName(command)

            Then("저장이 끝난 뒤 카탈로그 캐시를 비운다") {
                verifyOrder {
                    collegePort.updateDisplayName("ENGINEERING", displayName)
                    catalogQueryUseCase.refresh()
                }
            }

            Then("바뀐 표시명과 소속 학과 수를 돌려준다") {
                detail.college.displayName shouldBe displayName
                detail.topicCount shouldBe 14L
            }
        }

        When("없거나 삭제된 단과대면") {
            clearMocks(collegePort, catalogQueryUseCase)
            every {
                collegePort.updateDisplayName(any(), any())
            } throws CollegeException(CollegeErrorCode.COLLEGE_NOT_FOUND)

            Then("COLLEGE_NOT_FOUND 예외를 던지고 캐시는 비우지 않는다") {
                shouldThrow<CollegeException> {
                    service.updateDisplayName(command)
                }.baseErrorCode shouldBe CollegeErrorCode.COLLEGE_NOT_FOUND
                verify(exactly = 0) {
                    catalogQueryUseCase.refresh()
                }
            }
        }
    }

})
