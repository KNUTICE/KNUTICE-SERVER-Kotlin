package com.fx.global.adapter.out.persistence.document;

import com.fx.global.adapter.out.persistence.base.MongoBaseDocument;
import com.fx.global.domain.DailyApiLogStatistics;
import com.fx.global.domain.DailyStatistics;
import com.querydsl.core.annotations.QueryEntity;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "daily_api_log_statistics")
@SuperBuilder
@NoArgsConstructor
@QueryEntity
@CompoundIndex(name = "idx_stats_date_url", def = "{'statisticsDate': 1, 'urlPattern': 1}")
public class DailyApiLogStatisticsDocument extends MongoBaseDocument {

    @Id
    private String id;

    private LocalDate statisticsDate;

    private String urlPattern;

    private String method;

    private Long totalCount;

    private Long errorCount;

    private Double averageExecutionTime;

    public static DailyApiLogStatisticsDocument from(DailyApiLogStatistics domain) {
        return DailyApiLogStatisticsDocument.builder()
            .id(domain.getId())
            .statisticsDate(domain.getStatisticsDate())
            .urlPattern(domain.getUrlPattern())
            .method(domain.getMethod())
            .totalCount(domain.getTotalCount())
            .errorCount(domain.getErrorCount())
            .averageExecutionTime(domain.getAverageExecutionTime())
            .build();
    }

    public DailyApiLogStatistics toDomain(LocalDate statisticsDate) {
        return new DailyApiLogStatistics(
            this.id,
            statisticsDate,
            this.urlPattern,
            this.method,
            this.totalCount,
            this.errorCount,
            this.averageExecutionTime,
            this.createdAt,
            this.updatedAt
        );
    }

}