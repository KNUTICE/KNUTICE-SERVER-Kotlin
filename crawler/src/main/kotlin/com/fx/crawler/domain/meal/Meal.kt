package com.fx.crawler.domain.meal

import java.time.LocalDate

/** 하루치 식단. 메뉴는 학교 원문 그대로다. */
data class Meal(
    val topicCode: Int,
    val mealDate: LocalDate,
    val koreaMenus: List<String>,
    val topMenus: List<String>,
)
