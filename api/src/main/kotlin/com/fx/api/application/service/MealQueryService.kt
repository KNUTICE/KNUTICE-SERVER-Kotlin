package com.fx.api.application.service

import com.fx.api.application.port.`in`.MealQueryUseCase
import com.fx.api.application.port.out.MealRemotePort
import com.fx.global.domain.Meal
import com.fx.global.domain.MealType
import com.fx.global.exception.MealException
import com.fx.global.exception.errorcode.MealErrorCode
import org.springframework.stereotype.Service

@Service
class MealQueryService(
    private val mealRemotePort: MealRemotePort
) : MealQueryUseCase {

    override suspend fun getMeals(type: MealType): List<Meal> {
        val meals = mealRemotePort.getMeals(type)
        if (meals.isEmpty()) {
            throw MealException(MealErrorCode.MEAL_NOT_FOUND)
        }
        return meals
    }

}

