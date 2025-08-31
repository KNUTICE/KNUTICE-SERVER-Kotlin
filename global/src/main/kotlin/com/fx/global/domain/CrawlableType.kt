package com.fx.global.domain

interface CrawlableType {
    val rootDomain: String
    val bbsPath: String
    val category: String
    val typeName: String // DB에 저장할 이름 ex) GENERAL_NEWS, EVENT_NEWS....
    fun getNoticeUrl(): String = "$rootDomain$bbsPath"

    companion object {
        // ex) {GENERAL_NEWS: NoticeType.GENERAL_NEWS, COMPUTER_SCIENCE: MajorType.COMPUTER_SCIENCE...}
        private val registry = mutableMapOf<String, CrawlableType>()

        fun register(type: CrawlableType) {
            registry[type.typeName] = type
        }

        @JvmStatic
        fun fromString(typeName: String): CrawlableType =
            registry[typeName] ?: throw IllegalArgumentException("Unknown type: $typeName")

        @JvmStatic
        fun allTypeNames(): Set<String> = registry.keys

    }

}