package com.fx.api.adapter.out.persistence.document;

import com.fx.api.domain.Report;
import com.fx.global.adapter.out.persistence.base.MongoBaseDocument;
import com.querydsl.core.annotations.QueryEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "report")
@SuperBuilder
@NoArgsConstructor
public class ReportDocument extends MongoBaseDocument {

    @Id
    private String id;

    private String fcmToken;

    private String content;

    private String deviceName;

    private String version;

    public static ReportDocument from(Report report) {
        return ReportDocument.builder()
            .id(report.getId())
            .fcmToken(report.getFcmToken())
            .content(report.getContent())
            .deviceName(report.getDeviceName())
            .version(report.getVersion())
            .build();
    }

}
