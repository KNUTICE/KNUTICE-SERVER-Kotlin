package com.fx.api.application.port.out

import com.fx.common.domain.Meal
import com.fx.common.domain.MealType

interface MealRemotePort {

    suspend fun getMeals(type: MealType): List<Meal>

}


