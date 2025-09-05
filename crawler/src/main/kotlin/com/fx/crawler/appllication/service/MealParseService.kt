package com.fx.crawler.appllication.service

import com.fx.crawler.appllication.port.`in`.MealParseUseCase
import com.fx.crawler.appllication.port.out.MealParsePort
import com.fx.global.domain.Meal
import com.fx.global.domain.MealType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MealParseService(
    private val mealParsePort: MealParsePort
) : MealParseUseCase {

    private val log = LoggerFactory.getLogger(MealParseService::class.java)

    override suspend fun parseMeals(types: List<MealType>): List<Meal> = supervisorScope {
        types.map {
            async {
                log.info("Parse target: {}", it)
                try {
                    mealParsePort.parseMeal(it)
                } catch (e: Exception) {
                    log.info("{} 파싱 실패 : {}", it, e.message)
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

}