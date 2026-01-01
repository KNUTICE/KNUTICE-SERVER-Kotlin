package com.fx.crawler.appllication.port.out

import com.fx.global.domain.Meal
import com.fx.global.domain.MealType

interface MealParsePort {

    fun parseMeal(topic: MealType): Meal?

}