package com.fx.global.application.port.out

import com.fx.global.domain.SlackMessage

interface WebhookPort {

    suspend fun notifySlack(slackMessage: SlackMessage)

}