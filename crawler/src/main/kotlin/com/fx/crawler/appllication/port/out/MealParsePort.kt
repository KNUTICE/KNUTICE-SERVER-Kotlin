package com.fx.crawler.appllication.port.out

import com.fx.common.domain.Meal
import com.fx.common.domain.MealType

interface MealParsePort {

    fun parseMeal(topic: MealType): Meal?

}