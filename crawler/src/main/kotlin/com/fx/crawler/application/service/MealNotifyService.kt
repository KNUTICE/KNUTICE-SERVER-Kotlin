package com.fx.crawler.application.service

import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.concurrent.BoundedParallelExecutor
import com.fx.common.domain.TopicType
import com.fx.crawler.application.port.`in`.MealNotifyUseCase
import com.fx.crawler.application.port.out.MealPort
import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.meal.Meal
import com.fx.crawler.domain.push.MealPushComposer
import com.fx.crawler.domain.push.TopicPushPlan
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.time.Clock
import java.time.LocalDate

@Service
class MealNotifyService(
    private val catalogQueryUseCase: CatalogQueryUseCase,
    private val mealPort: MealPort,
    @param:Qualifier("schoolSiteExecutor") private val schoolSiteExecutor: BoundedParallelExecutor,
    private val properties: CrawlerProperties,
    private val clock: Clock,
) : MealNotifyUseCase {

    private val log = LoggerFactory.getLogger(MealNotifyService::class.java)

    override fun fetchTodayMeals(): List<Meal> {
        val today = LocalDate.now(clock)
        val topics = catalogQueryUseCase.getTopicCatalog().topicsOf(TopicType.MEAL).filter { it.crawlEnabled }

        val results = schoolSiteExecutor.invokeAll(
            topics.map { topic -> { mealPort.fetchMeal(topic, today) } },
            properties.crawl.timeout,
        )
        return topics.zip(results).mapNotNull { (topic, result) ->
            result.getOrElse {
                log.error("식단 조회 실패 - topic: {}, {}", topic.name, it.message, it)
                null
            }
        }
    }

    override fun preparePushPlans(meals: List<Meal>): List<TopicPushPlan> {
        val catalog = catalogQueryUseCase.getTopicCatalog()
        val templates = catalogQueryUseCase.getNotificationTemplateCatalog()

        return meals.mapNotNull { meal ->
            val topic = catalog.findByCode(meal.topicCode) ?: return@mapNotNull null
            TopicPushPlan(topicCode = meal.topicCode, messages = MealPushComposer.compose(topic, meal, templates))
        }
    }

}
