package com.fx.api.adapter.out.web

import com.fx.api.application.port.out.MealRemotePort
import com.fx.global.annotation.hexagonal.WebOutputAdapter
import com.fx.global.domain.Meal
import com.fx.global.domain.MealType

@WebOutputAdapter
class MealRemoteAdapter(
) : MealRemotePort {

    override suspend fun getMeals(type: MealType): List<Meal> {
        TODO("Not yet implemented")
    }

}

