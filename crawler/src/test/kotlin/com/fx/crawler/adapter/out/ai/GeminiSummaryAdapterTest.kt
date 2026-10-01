package com.fx.crawler.adapter.out.ai

import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.summary.SummaryRateLimitedException
import com.openai.core.http.Headers
import com.openai.errors.BadRequestException
import com.openai.errors.InternalServerException
import com.openai.errors.RateLimitException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class GeminiSummaryAdapterTest {

    private val chatClient = mockk<ChatClient>()
    private val clock = MutableClock(Instant.parse("2026-10-01T03:00:00Z"))
    private val adapter = GeminiSummaryAdapter(
        chatClient,
        CrawlerProperties(
            summary = CrawlerProperties.Summary(requestInterval = Duration.ZERO, rateLimitCooldown = Duration.ofMinutes(1)),
        ),
        clock,
    )

    @Test
    fun `호출 한도에 걸리면 쿨다운 동안 요청하지 않는다`() {
        every {
            chatClient.prompt(any<Prompt>()).call().content()
        } throws RateLimitException.builder().headers(Headers.builder().build()).build()

        assertThatThrownBy {
            adapter.summarize("본문")
        }.isInstanceOf(SummaryRateLimitedException::class.java)
        assertThatThrownBy {
            adapter.summarize("본문")
        }.isInstanceOf(SummaryRateLimitedException::class.java)

        // 한도 응답 뒤에는 다시 시도하지도, 다음 공지를 보내지도 않는다
        verify(exactly = 1) {
            chatClient.prompt(any<Prompt>())
        }

        every {
            chatClient.prompt(any<Prompt>()).call().content()
        } returns "요약"
        clock.now = clock.now.plus(Duration.ofMinutes(1))

        assertThat(adapter.summarize("본문")).isEqualTo("요약")
    }

    @Test
    fun `요청이 잘못된 오류는 다시 시도하지 않는다`() {
        every {
            chatClient.prompt(any<Prompt>()).call().content()
        } throws BadRequestException.builder().headers(Headers.builder().build()).build()

        assertThatThrownBy {
            adapter.summarize("본문")
        }.isInstanceOf(BadRequestException::class.java)
        verify(exactly = 1) {
            chatClient.prompt(any<Prompt>())
        }
    }

    @Test
    fun `서버 오류는 다시 시도한다`() {
        every {
            chatClient.prompt(any<Prompt>()).call().content()
        } throws InternalServerException.builder().statusCode(503).headers(Headers.builder().build()).build() andThen "요약"

        assertThat(adapter.summarize("본문")).isEqualTo("요약")
        verify(exactly = 2) {
            chatClient.prompt(any<Prompt>())
        }
    }

    private class MutableClock(var now: Instant) : Clock() {
        override fun instant(): Instant =
            now
        override fun getZone(): ZoneId =
            ZoneOffset.UTC
        override fun withZone(zone: ZoneId?): Clock =
            this
    }

}
