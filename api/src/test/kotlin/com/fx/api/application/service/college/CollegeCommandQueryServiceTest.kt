package com.fx.api.application.service.college

import com.fx.api.application.port.out.college.CollegePersistencePort
import com.fx.common.domain.college.College
import com.fx.common.domain.i18n.LocalizedText
import com.fx.persistence.withId
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class CollegeCommandQueryServiceTest : BehaviorSpec({

    val collegePort = mockk<CollegePersistencePort>()
    val service = CollegeCommandQueryService(collegePort)

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

})
