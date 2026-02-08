package com.fx.api.adapter.`in`.web.dto.meal

import com.fx.global.domain.Meal
import java.time.LocalDate

/**
 * 학시 응답 DTO
 *
 * @author 이동섭
 * @since 2026-02-08
 */
data class MealResponse(

    val mealDate: LocalDate,
    val koreaFood: List<String>,
    val topFood: List<String>

) {
    companion object {
        fun from(meal: Meal): MealResponse =
            MealResponse(
                mealDate = meal.mealDate,
                koreaFood = meal.koreaMenus.orEmpty(),
                topFood = meal.topMenus.orEmpty()
            )

        fun from(meals: List<Meal>): List<MealResponse> =
            meals.map { this.from(it) }
    }

}
