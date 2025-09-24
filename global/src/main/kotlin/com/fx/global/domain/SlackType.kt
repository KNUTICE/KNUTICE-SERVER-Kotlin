package com.fx.global.domain

enum class SlackType(
    val title: String
) {

    REPORT("문의사항"),
    CRAWL_ERROR("크롤링 중 오류발생"),
    FCM_ERROR("메시지 전송 오류 발생"),
    AI_ERROR("AI 요약 오류 발생"),
    ERROR("오류발생"),

}