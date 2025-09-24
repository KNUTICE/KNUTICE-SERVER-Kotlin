package com.fx.crawler.config.gemini

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.model.ChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GeminiChatClientConfig {

    @Bean
    fun chatClient(chatModel: ChatModel): ChatClient {
        return ChatClient.create(chatModel)
    }

}