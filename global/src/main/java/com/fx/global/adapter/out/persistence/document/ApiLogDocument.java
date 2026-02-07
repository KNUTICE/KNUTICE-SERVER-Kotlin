package com.fx.global.adapter.out.persistence.document;

import com.fx.global.adapter.out.persistence.base.MongoBaseDocument;
import com.fx.global.domain.ApiLog;
import com.fx.global.domain.DeviceType;
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

    private DeviceType deviceType;

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
            .deviceType(apiLog.getDeviceType())
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
            this.deviceType,
            this.statusCode,
            this.executionTime,
            this.createdAt,
            this.updatedAt
        );
    }

}