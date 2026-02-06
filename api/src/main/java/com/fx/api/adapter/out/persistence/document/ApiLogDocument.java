package com.fx.api.adapter.out.persistence.document;

import com.fx.api.domain.ApiLog;
import com.fx.global.adapter.out.persistence.base.MongoBaseDocument;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "api_log")
@SuperBuilder
@NoArgsConstructor
public class ApiLogDocument extends MongoBaseDocument {

    @Id
    private String id;

    private String urlPattern;

    private String url;

    private String method;

    private Map<String, String> queryParameters;

    private String fcmToken;

    private String clientIp;

    private Integer statusCode;

    private Long executionTime;

    public static ApiLogDocument from(ApiLog apiLog) {
        return ApiLogDocument.builder()
            .id(apiLog.getId())
            .urlPattern(apiLog.getUrlPattern())
            .url(apiLog.getUrl())
            .method(apiLog.getMethod())
            .queryParameters(apiLog.getQueryParameters())
            .fcmToken(apiLog.getFcmToken())
            .clientIp(apiLog.getClientIp())
            .statusCode(apiLog.getStatusCode())
            .executionTime(apiLog.getExecutionTime())
            .build();
    }

    public ApiLog toDomain() {
        return new ApiLog(
            this.id,
            this.urlPattern,
            this.url,
            this.method,
            this.queryParameters,
            this.fcmToken,
            this.clientIp,
            this.statusCode,
            this.executionTime,
            this.createdAt,
            this.updatedAt
        );
    }

}