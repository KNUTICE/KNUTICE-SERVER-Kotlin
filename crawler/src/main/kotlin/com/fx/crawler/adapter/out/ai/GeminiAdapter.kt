package com.fx.crawler.adapter.out.ai

import com.fx.crawler.appllication.port.out.NoticeSummaryPort
import com.fx.global.domain.Notice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.stereotype.Component

@Component
class GeminiAdapter(
    private val chatClient: ChatClient
) : NoticeSummaryPort {

    private val log = LoggerFactory.getLogger(GeminiAdapter::class.java)

    override suspend fun summarizeNotices(notices: List<Notice>): List<Notice> = coroutineScope {
        val jobs = notices.mapNotNull { notice ->
            if (notice.content.isNullOrBlank()) null
            else async(Dispatchers.IO) {
                log.info("start ai summary : ${notice.nttId}")
                retrySummaryNotice(notice)
            }
        }
        jobs.awaitAll().filterNotNull()
    }

    private suspend fun retrySummaryNotice(
        notice: Notice,
        maxRetries: Int = 3,
    ) : Notice? {
        var attempt = 0
        var waitTime = 1L

        val prompt = Prompt("해당 내용 마크다운으로 간결하게 요약해줘 : ${notice.content}")
        while (attempt < maxRetries) {
            attempt++
            try {
                val contentSummary = chatClient.prompt(prompt).call().content()
                return notice.copy(contentSummary = contentSummary)
            } catch (e: Exception) { // NonTransientAiException, TransientAiException
                if (e.message?.contains("429", ignoreCase = true) == true &&
                    e.message?.contains("quota", ignoreCase = true) == true) {
                    log.error("Quota 초과로 재시도 불가: nttId=${notice.nttId}")
                    return null // 종료
                }

                log.error("에러 발생 : ${notice.nttId}")
                if (attempt < maxRetries) {
                    log.warn("일시적 오류 발생, ${waitTime}초 후 재시도 ($attempt/$maxRetries)")
                    delay(waitTime * 1000)
                    waitTime *= 2
                }
            }

        }
        log.warn("최대 재시도 횟수($maxRetries) 도달, 최종 실패: ${notice.nttId}")
        return null
    }

}