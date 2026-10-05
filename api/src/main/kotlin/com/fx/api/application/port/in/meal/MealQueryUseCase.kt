package com.fx.api.application.port.`in`.meal

import com.fx.api.domain.Meal

interface MealQueryUseCase {

    /** [mealTopicName] 학식 토픽(예: `STUDENT_CAFETERIA`)의 식단. */
    fun getMeals(mealTopicName: String): List<Meal>

}
