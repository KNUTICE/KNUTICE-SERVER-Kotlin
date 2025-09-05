package com.fx.global.domain

import java.time.LocalDate

data class Meal(

    val mealDate: LocalDate,
    val menus: List<String>,
    val type: MealType,

)
