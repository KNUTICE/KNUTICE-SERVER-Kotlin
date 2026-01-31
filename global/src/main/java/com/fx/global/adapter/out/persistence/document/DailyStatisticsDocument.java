package com.fx.global.adapter.out.persistence.document;

import com.fx.global.adapter.out.persistence.base.MongoBaseDocument;
import com.fx.global.domain.DailyStatistics;
import com.querydsl.core.annotations.QueryEntity;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "daily_statistics")
@SuperBuilder
@NoArgsConstructor
@QueryEntity
public class DailyStatisticsDocument extends MongoBaseDocument {

    @Id
    private LocalDate statisticsDate;

    // 공지
    @Builder.Default
    private Long noticeCount = 0L; // 전체 공지 수
    @Builder.Default
    private Long noticeSummaryCount = 0L; // AI 요약된 공지 수

    // 리포트
    @Builder.Default
    private Long reportCount = 0L; // 전체 리포트 수

    // FCM 활성 토큰
    @Builder.Default
    private Long fcmTokenActiveCount = 0L;
    @Builder.Default
    private Long fcmTokenActiveAosCount = 0L;
    @Builder.Default
    private Long fcmTokenActiveIosCount = 0L;

    // FCM 비활성 토큰
    @Builder.Default
    private Long fcmTokenInactiveCount = 0L;
    @Builder.Default
    private Long fcmTokenInactiveAosCount = 0L;
    @Builder.Default
    private Long fcmTokenInactiveIosCount = 0L;

    public DailyStatistics toDomain() {
        return new DailyStatistics(
            this.statisticsDate,
            this.noticeCount,
            this.noticeSummaryCount,
            this.reportCount,
            this.fcmTokenActiveCount,
            this.fcmTokenActiveAosCount,
            this.fcmTokenActiveIosCount,
            this.fcmTokenInactiveCount,
            this.fcmTokenInactiveAosCount,
            this.fcmTokenInactiveIosCount,
            this.createdAt,
            this.updatedAt
        );
    }

    public static DailyStatisticsDocument from(DailyStatistics dailyStatistics) {
        return DailyStatisticsDocument.builder()
            .statisticsDate(dailyStatistics.getStatisticsDate())
            .noticeCount(dailyStatistics.getNoticeCount())
            .noticeSummaryCount(dailyStatistics.getNoticeSummaryCount())
            .reportCount(dailyStatistics.getReportCount())
            .fcmTokenActiveCount(dailyStatistics.getFcmTokenActiveCount())
            .fcmTokenActiveAosCount(dailyStatistics.getFcmTokenActiveAosCount())
            .fcmTokenActiveIosCount(dailyStatistics.getFcmTokenActiveIosCount())
            .fcmTokenInactiveCount(dailyStatistics.getFcmTokenInactiveCount())
            .fcmTokenInactiveAosCount(dailyStatistics.getFcmTokenInactiveAosCount())
            .fcmTokenInactiveIosCount(dailyStatistics.getFcmTokenInactiveIosCount())
            .createdAt(dailyStatistics.getCreatedAt())
            .build();
    }

}

