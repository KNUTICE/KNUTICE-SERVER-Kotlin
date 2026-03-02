package com.fx.global.adapter.out.persistence.document

import com.fx.global.adapter.out.persistence.base.MongoBaseDocument
import com.fx.global.domain.DailyApiLogStatistics
import com.querydsl.core.annotations.QueryEntity
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document(collection = "daily_api_log_statistics")
@QueryEntity
@CompoundIndex(
    name = "idx_stats_date_url",
    def = "{'statisticsDate': 1, 'urlPattern': 1}"
)
class DailyApiLogStatisticsDocument(

    @Id
    var id: String? = null,

    var statisticsDate: LocalDate,

    var urlPattern: String,

    var method: String,

    var totalCount: Long,

    var errorCount: Long,

    var averageExecutionTime: Double

) : MongoBaseDocument() {

    fun toDomain(): DailyApiLogStatistics =
        DailyApiLogStatistics(
            id = id,
            statisticsDate = statisticsDate,
            urlPattern = urlPattern,
            method = method,
            totalCount = totalCount,
            errorCount = errorCount,
            averageExecutionTime = averageExecutionTime,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    companion object {
        fun from(domain: DailyApiLogStatistics): DailyApiLogStatisticsDocument =
            DailyApiLogStatisticsDocument(
                id = domain.id,
                statisticsDate = domain.statisticsDate,
                urlPattern = domain.urlPattern,
                method = domain.method,
                totalCount = domain.totalCount,
                errorCount = domain.errorCount,
                averageExecutionTime = domain.averageExecutionTime
            ).apply {
                this.createdAt = domain.createdAt
                this.updatedAt = domain.updatedAt
            }
    }

}