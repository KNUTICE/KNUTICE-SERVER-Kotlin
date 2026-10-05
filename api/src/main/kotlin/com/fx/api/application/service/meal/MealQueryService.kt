package com.fx.api.application.service.meal

import com.fx.api.application.port.`in`.meal.MealQueryUseCase
import com.fx.api.application.port.out.meal.MealRemotePort
import com.fx.api.application.service.topic.TopicResolver
import com.fx.api.domain.Meal
import com.fx.common.domain.TopicType
import com.fx.common.exception.MealException
import com.fx.common.exception.errorcode.MealErrorCode
import org.springframework.stereotype.Service

/** 식단은 학교 사이트에서 바로 읽고 저장하지 않는다. */
@Service
class MealQueryService(
    private val mealRemotePort: MealRemotePort,
    private val topicResolver: TopicResolver,
) : MealQueryUseCase {

    override fun getMeals(mealTopicName: String): List<Meal> {
        val mealTopic = topicResolver.byName(mealTopicName, TopicType.MEAL)
        val meals = mealRemotePort.getMeals(mealTopic)
        if (meals.isEmpty()) {
            throw MealException(MealErrorCode.MEAL_NOT_FOUND)
        }
        return meals
    }

}
