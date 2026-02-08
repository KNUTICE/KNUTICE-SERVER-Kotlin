package com.fx.api.application.port.out

import com.fx.global.domain.Meal
import com.fx.global.domain.MealType

interface MealRemotePort {

    suspend fun getMeals(type: MealType): List<Meal>

}


