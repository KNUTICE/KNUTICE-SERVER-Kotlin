package com.fx.crawler.adapter.out.push

import com.fx.common.application.port.out.WebhookPort
import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.push.PushMessage
import com.fx.crawler.domain.push.PushTarget
import com.google.firebase.messaging.BatchResponse
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.MessagingErrorCode
import com.google.firebase.messaging.SendResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FcmPushAdapterTest {

    private val firebaseMessaging = mockk<FirebaseMessaging>()
    private val webhookPort = mockk<WebhookPort>(relaxed = true)
    private val adapter = FcmPushAdapter(firebaseMessaging, webhookPort, CrawlerProperties(push = CrawlerProperties.Push(maxAttempts = 2)))
    private val message = PushMessage("일반소식", "공지")

    private fun targets(count: Int) =
        (1..count).map {
            PushTarget(it.toLong(), "token-$it", "ko")
        }

    private fun success(): SendResponse =
        mockk {
            every {
                isSuccessful
            } returns true
        }

    private fun failure(code: MessagingErrorCode): SendResponse {
        val exception = mockk<FirebaseMessagingException> {
            every {
                messagingErrorCode
            } returns code
        }
        return mockk {
            every {
                isSuccessful
            } returns false
            every {
                this@mockk.exception
            } returns exception
        }
    }

    private fun response(vararg results: SendResponse): BatchResponse =
        mockk {
            every {
                responses
            } returns results.toList()
        }

    @Test
    fun `등록이 풀린 토큰은 돌려주고 일시 오류 토큰만 다시 보낸다`() {
        every {
            firebaseMessaging.sendEachForMulticast(any())
        } returnsMany listOf(
            response(success(), failure(MessagingErrorCode.UNREGISTERED), failure(MessagingErrorCode.UNAVAILABLE)),
            response(success()),
        )

        val invalid = adapter.send(targets(3), message)

        assertThat(invalid).containsExactly(2L)
        verify(exactly = 2) {
            firebaseMessaging.sendEachForMulticast(any())
        }
        verify(exactly = 0) {
            webhookPort.notifySlack(any())
        }
    }

    @Test
    fun `일시 오류가 한도까지 계속되면 Slack 으로 알리고 더 보내지 않는다`() {
        every {
            firebaseMessaging.sendEachForMulticast(any())
        } returns response(failure(MessagingErrorCode.INTERNAL))

        val invalid = adapter.send(targets(1), message)

        assertThat(invalid).isEmpty()
        verify(exactly = 2) {
            firebaseMessaging.sendEachForMulticast(any())
        }
        verify(exactly = 1) {
            webhookPort.notifySlack(any())
        }
    }

    @Test
    fun `전송 예외를 던지지 않고 Slack 으로 알린다`() {
        every {
            firebaseMessaging.sendEachForMulticast(any())
        } throws IllegalStateException("인증 실패")

        val invalid = adapter.send(targets(2), message)

        assertThat(invalid).isEmpty()
        verify(exactly = 1) {
            webhookPort.notifySlack(any())
        }
    }

    @Test
    fun `500개씩 나눠 보낸다`() {
        every {
            firebaseMessaging.sendEachForMulticast(any())
        } answers {
            response(*Array(if (callCount() == 1) 500 else 1) {
                success()
            })
        }

        adapter.sendSilent(targets(501))

        verify(exactly = 2) {
            firebaseMessaging.sendEachForMulticast(any())
        }
    }

    private var calls = 0
    private fun callCount(): Int =
        ++calls

}
