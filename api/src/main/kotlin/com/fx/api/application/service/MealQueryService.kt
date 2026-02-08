package com.fx.api.application.service

import com.fx.api.application.port.`in`.MealQueryUseCase
import com.fx.api.application.port.out.MealRemotePort
import com.fx.global.domain.Meal
import com.fx.global.domain.MealType

class MealQueryService(
    private val mealRemotePort: MealRemotePort
) : MealQueryUseCase {

    override suspend fun getMeals(mealType: MealType): List<Meal> {
        TODO("Not yet implemented")
    }

}

