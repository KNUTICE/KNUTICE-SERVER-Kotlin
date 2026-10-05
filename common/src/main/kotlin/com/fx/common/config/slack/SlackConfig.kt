package com.fx.common.config.slack

import com.slack.api.Slack
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SlackConfig {

    @Bean
    fun slack(): Slack =
        Slack.getInstance()

}