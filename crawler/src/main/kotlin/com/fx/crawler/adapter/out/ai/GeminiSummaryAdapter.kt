package com.fx.crawler.adapter.out.ai

import com.fx.crawler.application.port.out.NoticeSummaryPort
import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.summary.SummaryRateLimitedException
import com.openai.errors.OpenAIServiceException
import com.openai.errors.RateLimitException
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * Gemini(OpenAI 호환 API) 요약.
 *
 * - 요청은 `crawler.summary.request-interval` 이상 간격을 두고 보낸다 (재시도 포함). 분당 요청 한도를 넘지 않기 위해서다.
 *   SDK 의 자동 재시도는 이 간격을 거치지 않으므로 끈다 (`spring.ai.openai.chat.max-retries: 0`).
 * - 호출 한도 응답(429)을 받으면 `crawler.summary.rate-limit-cooldown` 동안 요청하지 않고 [SummaryRateLimitedException] 을 던진다.
 *   한도는 프로젝트 단위라 남은 공지를 보내도 거절되므로 요청을 아낀다.
 * - 서버 · 네트워크 오류는 1초 · 2초 뒤 다시 시도하고, 요청이 잘못된 오류(4xx)는 다시 보내도 같으므로 바로 실패시킨다.
 */
@Component
class GeminiSummaryAdapter(
    private val chatClient: ChatClient,
    private val properties: CrawlerProperties,
    private val clock: Clock,
) : NoticeSummaryPort {

    private val log = LoggerFactory.getLogger(GeminiSummaryAdapter::class.java)
    private val throttle = RequestThrottle(properties.summary.requestInterval, clock)

    @Volatile
    private var pausedUntil: Instant = Instant.MIN

    override fun summarize(content: String): String {
        checkNotPaused()
        val prompt = Prompt("해당 내용 마크다운으로 간결하게 요약해줘 : $content")
        var backoff = INITIAL_BACKOFF

        repeat(MAX_ATTEMPTS) { attempt ->
            throttle.acquire()
            try {
                return chatClient.prompt(prompt).call().content()
                    ?.takeIf {
                        it.isNotBlank()
                    }
                    ?: throw IllegalStateException("AI 가 빈 요약을 돌려줬습니다.")
            } catch (e: Exception) {
                if (e.hasCause<RateLimitException>()) {
                    pausedUntil = clock.instant().plus(properties.summary.rateLimitCooldown)
                    log.warn("AI 호출 한도 초과, {} 까지 요약 요청을 멈춤 - {}", pausedUntil, e.message)
                    throw SummaryRateLimitedException("AI 호출 한도 초과: ${e.message}", e)
                }
                if (!isRetryable(e) || attempt == MAX_ATTEMPTS - 1) {
                    throw e
                }
                log.warn("AI 요약 일시 오류, {}ms 후 다시 시도 ({}/{}) - {}", backoff.toMillis(), attempt + 1, MAX_ATTEMPTS, e.message)
                Thread.sleep(backoff)
                backoff = backoff.multipliedBy(2)
            }
        }
        error("도달할 수 없는 코드")
    }

    private fun checkNotPaused() {
        val until = pausedUntil
        if (clock.instant().isBefore(until)) {
            throw SummaryRateLimitedException("AI 호출 한도 초과로 $until 까지 요청하지 않습니다.")
        }
    }

    private fun isRetryable(e: Exception): Boolean =
        !(e is OpenAIServiceException && e.statusCode() in 400..499)

    private inline fun <reified T : Throwable> Throwable.hasCause(): Boolean =
        generateSequence(this) {
            it.cause
        }.any {
            it is T
        }

    companion object {
        private const val MAX_ATTEMPTS = 3
        private val INITIAL_BACKOFF: Duration = Duration.ofSeconds(1)
    }

}
