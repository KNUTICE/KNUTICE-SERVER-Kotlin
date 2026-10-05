package com.fx.crawler.adapter.`in`.batch.job

import com.fx.common.adapter.out.persistence.repository.FcmTokenRepository
import com.fx.common.domain.DeviceType
import com.fx.common.domain.fcmtoken.FcmToken
import com.fx.crawler.support.CrawlerIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.job.Job
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class SilentPushJobTest : CrawlerIntegrationTest() {

    @Autowired @Qualifier("silentPushJob") lateinit var silentPushJob: Job
    @Autowired lateinit var fcmTokenRepository: FcmTokenRepository

    @Test
    fun `활성 iOS 토큰에만 chunk 단위로 사일런트 푸시를 보내고 등록이 풀린 토큰은 비활성화한다`() {
        val iosTokens = (1..5).map {
            fcmTokenRepository.save(FcmToken("ios-$it", DeviceType.iOS, if (it % 2 == 0) "ja" else "ko"))
        }
        fcmTokenRepository.save(FcmToken("android", DeviceType.AOS))
        fcmTokenRepository.save(FcmToken("ios-inactive", DeviceType.iOS).apply {
            deactivate()
        })
        pushPort.invalidTokens += "ios-3"

        val execution = run(silentPushJob)

        assertThat(execution.status).isEqualTo(BatchStatus.COMPLETED)
        // chunk 크기 2 → 토큰 id 순으로 [1, 2] [3, 4] [5]
        assertThat(pushPort.sent.map {
            it.tokens
        }).containsExactly(listOf("ios-1", "ios-2"), listOf("ios-3", "ios-4"), listOf("ios-5"))
        assertThat(pushPort.sent).allMatch {
            it.message == null
        }
        assertThat(fcmTokenRepository.findById(requireNotNull(iosTokens[2].id)).orElseThrow().isActive).isFalse()
        assertThat(fcmTokenRepository.findAll().filter {
            !it.isActive
        }.map {
            it.token
        }).containsExactlyInAnyOrder("ios-3", "ios-inactive")
        assertThat(execution.stepExecutions.single().readCount).isEqualTo(5)
    }

}
