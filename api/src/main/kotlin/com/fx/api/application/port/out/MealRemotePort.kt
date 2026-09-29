package com.fx.api.application.port.out

import com.fx.api.domain.Meal
import com.fx.common.domain.catalog.TopicView

interface MealRemotePort {

    /** 학식 토픽의 식단 페이지에서 날짜별 메뉴를 읽는다. 메뉴가 없는 날은 뺀다. */
    fun getMeals(mealTopic: TopicView): List<Meal>

}
