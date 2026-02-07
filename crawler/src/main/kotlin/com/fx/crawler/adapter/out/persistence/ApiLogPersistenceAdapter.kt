package com.fx.crawler.adapter.out.persistence

import com.fx.crawler.appllication.port.out.ApiLogPersistencePort
import com.fx.global.adapter.out.persistence.document.DailyApiLogStatisticsDocument
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.DailyApiLogStatistics
import org.bson.Document
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationExpression
import org.springframework.data.mongodb.core.query.Criteria
import java.time.LocalDate
import java.time.LocalTime

@PersistenceAdapter
class ApiLogPersistenceAdapter(
    private val mongoTemplate: MongoTemplate,
) : ApiLogPersistencePort {

    override fun aggregateDailyStatistics(date: LocalDate): List<DailyApiLogStatistics> {

        val start = date.atStartOfDay()
        val end = date.atTime(LocalTime.MAX)

        val aggregation = Aggregation.newAggregation(

            // 1. 해당 날짜 로그만
            Aggregation.match(
                Criteria.where("createdAt").gte(start).lte(end)
            ),

            // 2. urlPattern + method 별 통계
            Aggregation.group("urlPattern", "method")
                .count().`as`("totalCount")
                .avg("executionTime").`as`("averageExecutionTime")
                .sum(
                    AggregationExpression { _ ->
                        Document(
                            "\$cond", listOf(
                                Document("\$gte", listOf("\$statusCode", 400)),
                                1,
                                0
                            )
                        )
                    }
                ).`as`("errorCount"),

            // 3. 결과 필드 정리
            Aggregation.project()
                .and("_id.urlPattern").`as`("urlPattern")
                .and("_id.method").`as`("method")
                .andInclude("totalCount", "averageExecutionTime", "errorCount")
                .and(AggregationExpression { Document("\$literal", date) })
                .`as`("statisticsDate")
        )

        val results = mongoTemplate.aggregate(
            aggregation,
            "api_log",
            DailyApiLogStatisticsDocument::class.java
        )

        return results.mappedResults.map { doc ->
            doc.toDomain().copy(
                id = "${date}_${doc.urlPattern}_${doc.method}",
            )
        }
    }

}