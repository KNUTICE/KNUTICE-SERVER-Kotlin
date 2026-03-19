package com.fx.global.adapter.out.webhook

import com.fx.global.application.port.out.WebhookPort
import com.fx.global.domain.SlackMessage
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class SlackWebhookAdapter(
    private val webClient: WebClient = WebClient.create(),
    @Value("\${webhook.slack.url}") private val slackWebhookUrl: String
) : WebhookPort {

    override suspend fun notifySlack(slackMessage: SlackMessage) {
        val message = "*_${slackMessage.type.title}_* :rotating_light:  \n${slackMessage.content}"

        val payload = mapOf("text" to message)
        try {
            webClient.post()
                .uri(slackWebhookUrl)
                .bodyValue(payload)
                .retrieve()
                .toBodilessEntity()
                .awaitSingleOrNull() // 코루틴 비동기 대기
        } catch (e: Exception) {
            throw RuntimeException("슬랙 에러 발생 : {}", e)
        }
    }

}