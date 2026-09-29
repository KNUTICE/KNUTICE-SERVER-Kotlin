package com.fx.crawler.application.port.`in`

import com.fx.crawler.domain.meal.Meal
import com.fx.crawler.domain.push.TopicPushPlan

interface MealNotifyUseCase {

    /** 크롤링 대상 식당들의 오늘 식단. 식단이 없거나 읽지 못한 식당은 빠진다. */
    fun fetchTodayMeals(): List<Meal>

    fun preparePushPlans(meals: List<Meal>): List<TopicPushPlan>

}
