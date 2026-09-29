package com.fx.api.domain

import java.time.LocalDate

/** 학교 식단 사이트에서 조회한 하루치 메뉴. 저장하지 않는다. */
data class Meal(
    val mealDate: LocalDate,
    val koreaMenus: List<String>,
    val topMenus: List<String>,
)
