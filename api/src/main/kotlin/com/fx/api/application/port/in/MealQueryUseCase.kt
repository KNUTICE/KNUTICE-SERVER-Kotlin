package com.fx.api.application.port.`in`

import com.fx.common.domain.Meal
import com.fx.common.domain.MealType

interface MealQueryUseCase {

    suspend fun getMeals(type: MealType): List<Meal>

}