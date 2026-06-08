package com.fx.global.domain

interface CrawlableType {
    val rootDomain: String
    val bbsPath: String
    val category: String
    val topicName: String // DB에 저장할 이름 ex) GENERAL_NEWS, EVENT_NEWS....
    val code: Int
    fun getNoticeUrl(): String = "$rootDomain$bbsPath"

    companion object {
        // ex) {GENERAL_NEWS: NoticeType.GENERAL_NEWS, COMPUTER_SCIENCE: MajorType.COMPUTER_SCIENCE...}
        private val registry = mutableMapOf<String, CrawlableType>()
        private val codeRegistry = mutableMapOf<Int, CrawlableType>()

        fun register(type: CrawlableType) {
            registry[type.topicName] = type
            codeRegistry[type.code] = type
        }

        @JvmStatic
        fun fromString(topicName: String): CrawlableType =
            registry[topicName] ?: throw IllegalArgumentException("Unknown type: $topicName")

        @JvmStatic
        fun fromCode(code: Int): CrawlableType =
            codeRegistry[code] ?: throw IllegalArgumentException("Unknown code: $code")

        @JvmStatic
        fun allTypeNames(): Set<String> = registry.keys

    }

}