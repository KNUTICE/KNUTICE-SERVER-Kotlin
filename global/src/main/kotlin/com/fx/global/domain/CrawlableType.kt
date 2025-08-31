package com.fx.global.domain

interface CrawlableType {
    val rootDomain: String
    val bbsPath: String
    val category: String
    val typeName: String // DB에 저장할 이름
    fun getNoticeUrl(): String = "$rootDomain$bbsPath"

    companion object {
        private val registry = mutableMapOf<String, CrawlableType>()

        fun register(type: CrawlableType) {
            registry[type.typeName] = type
        }

        @JvmStatic
        fun fromString(typeName: String): CrawlableType =
            registry[typeName] ?: throw IllegalArgumentException("Unknown type: $typeName")
    }

}