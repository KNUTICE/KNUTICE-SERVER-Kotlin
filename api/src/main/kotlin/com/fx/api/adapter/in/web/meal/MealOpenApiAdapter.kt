package com.fx.api.adapter.`in`.web.meal

import com.fx.api.adapter.`in`.web.meal.dto.MealResponse
import com.fx.api.application.port.`in`.meal.MealQueryUseCase
import com.fx.common.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/open-api/v1/meals")
class MealOpenApiAdapter(
    private val mealQueryUseCase: MealQueryUseCase
) {

    /** [type] 은 학식 토픽 이름 (예: `STUDENT_CAFETERIA`). */
    @GetMapping("/{type}")
    fun getMeals(@PathVariable type: String): ResponseEntity<Api<List<MealResponse>>> =
        Api.OK(MealResponse.from(mealQueryUseCase.getMeals(type)))

}
