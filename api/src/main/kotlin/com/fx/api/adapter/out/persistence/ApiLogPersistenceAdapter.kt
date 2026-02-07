package com.fx.api.adapter.out.persistence

import com.fx.global.adapter.out.persistence.document.ApiLogDocument
import com.fx.api.adapter.out.persistence.repository.ApiLogMongoRepository
import com.fx.api.application.port.out.ApiLogPersistencePort
import com.fx.api.domain.DailyTopicCount
import com.fx.global.domain.ApiLog
import com.fx.global.annotation.PersistenceAdapter
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.MongoExpression
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.query.Criteria
import java.time.LocalDateTime

@PersistenceAdapter
class ApiLogPersistenceAdapter(
    private val apiLogMongoRepository: ApiLogMongoRepository,
    private val mongoTemplate: MongoTemplate
): ApiLogPersistencePort {

    override fun save(apiLog: ApiLog) {
        apiLogMongoRepository.save(ApiLogDocument.from(apiLog))
    }

    override fun aggregateDailyTopicStatistics(start: LocalDateTime, end: LocalDateTime): List<DailyTopicCount> {
        val aggregation = Aggregation.newAggregation(

            // 1. notices API만
            match(
                Criteria.where("urlPattern").`is`("/open-api/v1/notices")
                    .and("createdAt").gte(start).lte(end)
                    .and("queryParameters.topic").exists(true)
            ),

            // 2. 날짜 추출
            project()
                .and(
                    AggregationExpression.from(
                        MongoExpression.create(
                            """
                { 
                  ${'$'}dateToString: { 
                    format: "%Y-%m-%d",
                    date: "${'$'}createdAt",
                    timezone: "Asia/Seoul"
                  }
                }
                """.trimIndent()
                        )
                    )
                ).`as`("date")
                .and("queryParameters.topic").`as`("topic"),

            // 3. 날짜 + topic 그룹
            group("date", "topic")
                .count().`as`("count"),

            // 4. 정리
            project()
                .and("_id.date").`as`("date")
                .and("_id.topic").`as`("topic")
                .and("count").`as`("count"),

            // 5. 정렬
            Aggregation.sort(
                Sort.by(
                    Sort.Order.desc("date"),
                    Sort.Order.asc("topic")
                )
            )
        )

        return mongoTemplate.aggregate(
            aggregation,
            "api_log",
            DailyTopicCount::class.java
        ).mappedResults
    }

}