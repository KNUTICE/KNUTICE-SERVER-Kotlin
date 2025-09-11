package com.fx.global.application.port.out

import com.fx.global.domain.SlackMessage

interface WebhookPort {

    fun notifySlack(slackMessage: SlackMessage)

}