package com.fx.common.application.port.out

import com.fx.common.domain.SlackMessage

interface WebhookPort {

    fun notifySlack(slackMessage: SlackMessage)

}