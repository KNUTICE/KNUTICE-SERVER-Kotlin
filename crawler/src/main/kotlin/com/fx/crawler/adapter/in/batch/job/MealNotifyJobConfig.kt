package com.fx.crawler.adapter.`in`.batch.job

import com.fx.common.domain.batch.BatchJobNames
import com.fx.crawler.adapter.`in`.batch.push.PushPartitionStepFactory
import com.fx.crawler.adapter.`in`.batch.push.TopicPushPartitioner
import com.fx.crawler.adapter.`in`.batch.support.CatalogRefreshJobListener
import com.fx.crawler.adapter.`in`.batch.support.StepTransactions
import com.fx.crawler.application.port.`in`.MealNotifyUseCase
import com.fx.crawler.domain.meal.Meal
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.job.Job
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.partition.Partitioner
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.Step
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.infrastructure.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue

/**
 * 학식 알림 Job.
 *
 * 1. 식단 조회 : 크롤링 대상 식당의 오늘 식단을 읽어 Job 컨텍스트(`meals`)에 담는다
 * 2. 발송 : 식당(토픽)별 파티션으로 나눠 구독자에게 보낸다 (공지 발송 Step 과 같다)
 */
@Configuration(proxyBeanMethods = false)
class MealNotifyJobConfig(
    private val jobRepository: JobRepository,
) {

    @Bean
    fun mealNotifyJob(
        catalogRefreshJobListener: CatalogRefreshJobListener,
        @Qualifier("mealFetchStep") mealFetchStep: Step,
        @Qualifier("mealPushStep") mealPushStep: Step,
    ): Job =
        JobBuilder(BatchJobNames.MEAL_NOTIFY, jobRepository)
            .listener(catalogRefreshJobListener)
            .start(mealFetchStep)
            .next(mealPushStep)
            .build()

    @Bean
    fun mealFetchStep(mealNotifyUseCase: MealNotifyUseCase, jsonMapper: JsonMapper): Step =
        StepBuilder("mealFetchStep", jobRepository)
            .tasklet({ contribution, chunkContext ->
                val meals = mealNotifyUseCase.fetchTodayMeals()
                chunkContext.stepContext.stepExecution.jobExecution.executionContext
                    .putString(MEALS_KEY, jsonMapper.writeValueAsString(meals))
                contribution.incrementWriteCount(meals.size.toLong())
                RepeatStatus.FINISHED
            }, StepTransactions.NONE)
            .build()

    @Bean
    fun mealPushStep(
        pushPartitionStepFactory: PushPartitionStepFactory,
        @Qualifier("mealPushPartitioner") mealPushPartitioner: Partitioner,
    ): Step =
        pushPartitionStepFactory.create("mealPushStep", mealPushPartitioner)

    @Bean
    @JobScope
    fun mealPushPartitioner(
        @Value("#{jobExecutionContext['meals']}") mealsJson: String?,
        mealNotifyUseCase: MealNotifyUseCase,
        jsonMapper: JsonMapper,
    ): Partitioner =
        TopicPushPartitioner(jsonMapper) {
            val meals = mealsJson?.let { jsonMapper.readValue<List<Meal>>(it) }.orEmpty()
            mealNotifyUseCase.preparePushPlans(meals)
        }

    companion object {
        const val MEALS_KEY = "meals"
    }

}
