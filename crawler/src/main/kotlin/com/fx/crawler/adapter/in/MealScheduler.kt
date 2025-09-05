package com.fx.crawler.adapter.`in`

import com.fx.crawler.appllication.port.`in`.MealNotificationUseCase
import com.fx.crawler.appllication.port.`in`.MealParseUseCase
import com.fx.crawler.common.annotation.ScheduleAdapter
import com.fx.global.domain.MealType
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled

@ScheduleAdapter
class MealScheduler(
    private val mealParseUseCase: MealParseUseCase,
    private val mealNotificationUseCase: MealNotificationUseCase
) {

    private val log = LoggerFactory.getLogger(MealScheduler::class.java)

    @Scheduled(cron = "0 10 10 * * *") // 매일 10시 10분마다
    fun parseAndPushMeals() = runBlocking {
        log.info("---------- Starting scheduled meal parse ----------")
        val meals = mealParseUseCase.parseMeals(MealType.entries)
        log.info("---------- Scheduled parse finished ----------")
        if (meals.isEmpty()) return@runBlocking

        log.info("---------- Starting meal notification ----------")
        mealNotificationUseCase.sendNotification(meals)
        log.info("---------- Meal notification completed ----------")
    }


}