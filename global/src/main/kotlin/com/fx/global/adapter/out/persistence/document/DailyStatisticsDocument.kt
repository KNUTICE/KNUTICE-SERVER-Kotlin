package com.fx.global.adapter.out.persistence.document

import com.fx.global.adapter.out.persistence.base.MongoBaseDocument
import com.fx.global.domain.DailyStatistics
import com.querydsl.core.annotations.QueryEntity
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document(collection = "daily_statistics")
@QueryEntity
class DailyStatisticsDocument(

    @Id
    var statisticsDate: LocalDate,

    // 공지
    var noticeCount: Long = 0L,
    var noticeSummaryCount: Long = 0L,

    // FCM 활성 토큰
    var fcmTokenActiveCount: Long = 0L,
    var fcmTokenActiveAosCount: Long = 0L,
    var fcmTokenActiveIosCount: Long = 0L,

    // FCM 비활성 토큰
    var fcmTokenInactiveCount: Long = 0L,
    var fcmTokenInactiveAosCount: Long = 0L,
    var fcmTokenInactiveIosCount: Long = 0L

) : MongoBaseDocument() {

    fun toDomain(): DailyStatistics =
        DailyStatistics(
            statisticsDate = statisticsDate,
            noticeCount = noticeCount,
            noticeSummaryCount = noticeSummaryCount,
            fcmTokenActiveCount = fcmTokenActiveCount,
            fcmTokenActiveAosCount = fcmTokenActiveAosCount,
            fcmTokenActiveIosCount = fcmTokenActiveIosCount,
            fcmTokenInactiveCount = fcmTokenInactiveCount,
            fcmTokenInactiveAosCount = fcmTokenInactiveAosCount,
            fcmTokenInactiveIosCount = fcmTokenInactiveIosCount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    companion object {
        fun from(domain: DailyStatistics): DailyStatisticsDocument =
            DailyStatisticsDocument(
                statisticsDate = domain.statisticsDate,
                noticeCount = domain.noticeCount,
                noticeSummaryCount = domain.noticeSummaryCount,
                fcmTokenActiveCount = domain.fcmTokenActiveCount,
                fcmTokenActiveAosCount = domain.fcmTokenActiveAosCount,
                fcmTokenActiveIosCount = domain.fcmTokenActiveIosCount,
                fcmTokenInactiveCount = domain.fcmTokenInactiveCount,
                fcmTokenInactiveAosCount = domain.fcmTokenInactiveAosCount,
                fcmTokenInactiveIosCount = domain.fcmTokenInactiveIosCount
            ).apply {
                this.createdAt = domain.createdAt
                this.updatedAt = domain.updatedAt
            }
    }

}