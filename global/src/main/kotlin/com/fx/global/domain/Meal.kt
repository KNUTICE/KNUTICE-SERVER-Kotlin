package com.fx.global.domain

import java.time.LocalDate

data class Meal(

    val mealDate: LocalDate,
    val koreaMenus: List<String>? = null,
    val topMenus: List<String>? = null,
    val topic: MealType,

)
