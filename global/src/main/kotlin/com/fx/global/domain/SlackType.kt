package com.fx.global.domain

enum class SlackType(
    val title: String
) {

    REPORT("문의사항"),
    CRAWL_ERROR("크롤링 중 오류발생"),
    ERROR("오류발생"),

}