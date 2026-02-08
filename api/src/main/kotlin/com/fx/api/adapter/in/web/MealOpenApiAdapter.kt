package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.meal.MealResponse
import com.fx.api.application.port.`in`.MealQueryUseCase
 import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.domain.MealType
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

    @GetMapping("/{type}")
    suspend fun getMeals(@PathVariable type: MealType): ResponseEntity<Api<List<MealResponse>>> =
        Api.OK(MealResponse.from(mealQueryUseCase.getMeals(type)))

}