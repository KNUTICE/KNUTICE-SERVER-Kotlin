package com.fx.crawler.adapter.out.ai

import com.fx.crawler.application.port.out.NoticeSummaryPort
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * Gemini(OpenAI 호환 API) 요약. 일시적인 오류는 1초 · 2초 간격으로 다시 시도하고,
 * 할당량 초과(429 quota)는 다시 시도해도 소용없으므로 바로 실패시킨다.
 */
@Component
class GeminiSummaryAdapter(
    private val chatClient: ChatClient,
) : NoticeSummaryPort {

    private val log = LoggerFactory.getLogger(GeminiSummaryAdapter::class.java)

    override fun summarize(content: String): String {
        val prompt = Prompt("해당 내용 마크다운으로 간결하게 요약해줘 : $content")
        var backoff = INITIAL_BACKOFF

        repeat(MAX_ATTEMPTS) { attempt ->
            try {
                return chatClient.prompt(prompt).call().content()
                    ?.takeIf { it.isNotBlank() }
                    ?: throw IllegalStateException("AI 가 빈 요약을 돌려줬습니다.")
            } catch (e: Exception) {
                if (isQuotaExceeded(e) || attempt == MAX_ATTEMPTS - 1) {
                    throw e
                }
                log.warn("AI 요약 일시 오류, {}ms 후 다시 시도 ({}/{}) - {}", backoff.toMillis(), attempt + 1, MAX_ATTEMPTS, e.message)
                Thread.sleep(backoff)
                backoff = backoff.multipliedBy(2)
            }
        }
        error("도달할 수 없는 코드")
    }

    private fun isQuotaExceeded(e: Exception): Boolean {
        val message = e.message ?: return false
        return message.contains("429") && message.contains("quota", ignoreCase = true)
    }

    companion object {
        private const val MAX_ATTEMPTS = 3
        private val INITIAL_BACKOFF: Duration = Duration.ofSeconds(1)
    }

}
