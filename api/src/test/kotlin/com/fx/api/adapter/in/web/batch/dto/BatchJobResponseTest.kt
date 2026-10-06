package com.fx.api.adapter.`in`.web.batch.dto

import com.fx.common.domain.batch.BatchJob
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class BatchJobResponseTest : BehaviorSpec({

    Given("Job 목록") {
        val responses = BatchJobResponse.from(BatchJob.entries).associateBy {
            it.jobName
        }

        Then("모든 Job 을 담는다") {
            responses.keys shouldBe setOf(
                "noticeCrawlJob", "noticeSummaryJob", "mealNotifyJob", "silentPushJob", "seatAlertCheckJob", "maintenanceJob",
            )
        }

        Then("파라미터 이름과 고를 수 있는 값을 담는다") {
            val topicType = responses.getValue("noticeCrawlJob").parameters.single()
            topicType.name shouldBe "topicType"
            topicType.allowedValues shouldBe listOf("NOTICE", "MAJOR")

            val retentionDays = responses.getValue("maintenanceJob").parameters.single()
            retentionDays.name shouldBe "retentionDays"
            retentionDays.allowedValues shouldBe null

            responses.getValue("silentPushJob").parameters shouldBe emptyList()
        }
    }

})
