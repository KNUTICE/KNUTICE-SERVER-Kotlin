package com.fx.api.application.service

import com.fx.api.application.port.`in`.MealQueryUseCase
import com.fx.api.application.port.out.MealRemotePort
import com.fx.common.domain.Meal
import com.fx.common.domain.MealType
import com.fx.common.exception.MealException
import com.fx.common.exception.errorcode.MealErrorCode
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

