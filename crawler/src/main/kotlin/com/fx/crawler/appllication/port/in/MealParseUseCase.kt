package com.fx.crawler.appllication.port.`in`

import com.fx.global.domain.Meal
import com.fx.global.domain.MealType

interface MealParseUseCase {

    suspend fun parseMeals(topics: List<MealType>): List<Meal>

    suspend fun parseMeal(topic: MealType): Meal?

}