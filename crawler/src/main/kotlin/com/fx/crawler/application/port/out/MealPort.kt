package com.fx.crawler.application.port.out

import com.fx.common.domain.catalog.TopicView
import com.fx.crawler.domain.meal.Meal
import java.time.LocalDate

/** 학교 식단 사이트 조회. */
interface MealPort {

    /** [date] 의 식단. 식단이 없거나 메뉴가 비어 있으면 null. */
    fun fetchMeal(topic: TopicView, date: LocalDate): Meal?

}
