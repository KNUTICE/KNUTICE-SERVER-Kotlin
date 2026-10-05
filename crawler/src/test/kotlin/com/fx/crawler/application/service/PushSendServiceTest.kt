package com.fx.crawler.application.service

import com.fx.common.domain.i18n.Language
import com.fx.crawler.application.port.out.FcmTokenPersistencePort
import com.fx.crawler.application.port.out.PushPort
import com.fx.crawler.domain.push.LocalizedPushMessages
import com.fx.crawler.domain.push.PushMessage
import com.fx.crawler.domain.push.PushTarget
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class PushSendServiceTest {

    private val pushPort = mockk<PushPort>()
    private val fcmTokenPersistencePort = mockk<FcmTokenPersistencePort>(relaxed = true)
    private val service = PushSendService(pushPort, fcmTokenPersistencePort, Clock.fixed(Instant.parse("2026-09-30T01:00:00Z"), ZoneId.of("Asia/Seoul")))

    private val ko1 = PushMessage("일반소식", "공지 1")
    private val ko2 = PushMessage("일반소식", "공지 2")
    private val ja = PushMessage("一般ニュース", "공지 1")
    private val en = PushMessage("General News", "공지 1")
    private val messages = LocalizedPushMessages(mapOf(Language.KO to listOf(ko1, ko2), Language.JA to listOf(ja), Language.EN to listOf(en)))

    @Test
    fun `토큰을 해석한 언어별로 묶어 그 언어의 알림을 보낸다`() {
        val koToken = PushTarget(1, "t1", "ko")
        val unknownLanguage = PushTarget(2, "t2", "zh")
        val jaJp = PushTarget(3, "t3", "ja-JP")
        val jaUpper = PushTarget(4, "t4", "JA")
        val enToken = PushTarget(5, "t5", "en")
        every {
            pushPort.send(any(), any())
        } returns emptyList()

        val result = service.send(listOf(koToken, unknownLanguage, jaJp, jaUpper, enToken), messages)

        verify(exactly = 1) {
            pushPort.send(listOf(koToken, unknownLanguage), ko1)
        }
        verify(exactly = 1) {
            pushPort.send(listOf(koToken, unknownLanguage), ko2)
        }
        verify(exactly = 1) {
            pushPort.send(listOf(jaJp, jaUpper), ja)
        }
        verify(exactly = 1) {
            pushPort.send(listOf(enToken), en)
        }
        verify(exactly = 0) {
            fcmTokenPersistencePort.deactivateAll(any(), any())
        }
        assertThat(result.targetCount).isEqualTo(5)
        assertThat(result.hasInvalidToken).isFalse()
    }

    @Test
    fun `등록이 풀린 토큰은 한 번에 비활성화한다`() {
        val koToken = PushTarget(1, "t1", "ko")
        val jaToken = PushTarget(3, "t3", "ja")
        every {
            pushPort.send(listOf(koToken), any())
        } returns listOf(1L)
        every {
            pushPort.send(listOf(jaToken), ja)
        } returns listOf(3L)

        val result = service.send(listOf(koToken, jaToken), messages)

        verify(exactly = 1) {
            fcmTokenPersistencePort.deactivateAll(setOf(1L, 3L), any())
        }
        assertThat(result.deactivatedTokenIds).containsExactlyInAnyOrder(1L, 3L)
    }

    @Test
    fun `사일런트 푸시도 등록이 풀린 토큰을 비활성화한다`() {
        val targets = listOf(PushTarget(1, "t1", "ko"), PushTarget(2, "t2", "ko"))
        every {
            pushPort.sendSilent(targets)
        } returns listOf(2L)

        service.sendSilent(targets)

        verify(exactly = 1) {
            fcmTokenPersistencePort.deactivateAll(setOf(2L), any())
        }
    }

}
