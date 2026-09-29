package com.fx.crawler.appllication.port.`in`

import com.fx.common.domain.Meal
import com.fx.common.domain.MealType

interface MealParseUseCase {

    suspend fun parseMeals(topics: List<MealType>): List<Meal>

    suspend fun parseMeal(topic: MealType): Meal?

}