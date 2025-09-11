package com.fx.global.domain

data class SlackMessage(

    val content: String,
    val type: SlackType

) {

    companion object {
        fun create(content: String, type: SlackType): SlackMessage =
            SlackMessage(
                content = content,
                type = type
            )
    }
}
