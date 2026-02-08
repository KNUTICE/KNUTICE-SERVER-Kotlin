package com.fx.api.application.port.`in`

import com.fx.global.domain.Meal
import com.fx.global.domain.MealType

interface MealQueryUseCase {

    suspend fun getMeals(type: MealType): List<Meal>

}