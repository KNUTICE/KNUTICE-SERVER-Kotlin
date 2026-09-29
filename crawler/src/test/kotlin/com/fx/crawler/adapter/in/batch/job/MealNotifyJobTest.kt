package com.fx.crawler.adapter.`in`.batch.job

import com.fx.common.adapter.out.persistence.repository.FcmTokenRepository
import com.fx.common.adapter.out.persistence.repository.FcmTokenSubscriptionRepository
import com.fx.common.domain.DeviceType
import com.fx.common.domain.fcmtoken.FcmToken
import com.fx.common.domain.fcmtoken.FcmTokenSubscription
import com.fx.crawler.domain.meal.Meal
import com.fx.crawler.support.CrawlerIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.Job
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import java.time.LocalDate

class MealNotifyJobTest : CrawlerIntegrationTest() {

    @Autowired @Qualifier("mealNotifyJob") lateinit var mealNotifyJob: Job
    @Autowired lateinit var fcmTokenRepository: FcmTokenRepository
    @Autowired lateinit var fcmTokenSubscriptionRepository: FcmTokenSubscriptionRepository

    @Test
    fun `오늘 식단이 있는 식당의 구독자에게만 발송한다`() {
        mealPort.meals[900] = Meal(900, LocalDate.MIN, koreaMenus = listOf("김치찌개"), topMenus = listOf("돈까스"))
        subscribe("student", 900)
        subscribe("staff", 901)

        val execution = run(mealNotifyJob)

        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        val sent = pushPort.sent.single()
        assertThat(sent.tokens).containsExactly("student")
        assertThat(sent.message!!.title).isEqualTo("학생식당")
        assertThat(sent.message!!.body).isEqualTo("${LocalDate.now()} 학생식당 메뉴\n[한식]\n김치찌개\n\n[일품]\n돈까스")
    }

    @Test
    fun `식단이 없으면 보내지 않는다`() {
        subscribe("student", 900)

        val execution = run(mealNotifyJob)

        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        assertThat(pushPort.sent).isEmpty()
    }

    private fun subscribe(token: String, topicCode: Int) {
        val saved = fcmTokenRepository.save(FcmToken(token, DeviceType.AOS))
        fcmTokenSubscriptionRepository.save(
            FcmTokenSubscription(requireNotNull(saved.id), topicCode, if (topicCode == 900) "STUDENT_CAFETERIA" else "STAFF_CAFETERIA")
        )
    }

}
