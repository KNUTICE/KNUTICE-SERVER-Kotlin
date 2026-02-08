package com.fx.api.adapter.`in`.web

import com.fx.api.application.port.out.MealRemotePort
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.domain.Meal
import com.fx.global.domain.MealType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/open-api/v1/meals")
class MealOpenApiAdapter(
    private val mealRemotePort: MealRemotePort
) {

    @GetMapping
    suspend fun getMeals(type: MealType): List<Meal> {
        return mealRemotePort.getMeals(type)
    }

}