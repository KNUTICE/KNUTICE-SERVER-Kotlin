package com.fx.global.adapter.out.persistence.document;

import com.fx.global.adapter.out.persistence.base.MongoBaseDocument;
import com.querydsl.core.annotations.QueryEntity;
import com.fx.global.domain.DeviceType;
import com.fx.global.domain.FcmToken;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "fcm_token")
@SuperBuilder
@NoArgsConstructor
@QueryEntity
public class FcmTokenDocument extends MongoBaseDocument {

    @Id
    private String fcmToken;

    @Builder.Default
    private Set<String> subscribedTopics = new HashSet<>();

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private DeviceType deviceType = DeviceType.UNKNOWN;

    public FcmToken toDomain() {
        return new FcmToken(
            this.fcmToken,
            this.subscribedTopics,
            this.deviceType,
            this.isActive,
            this.createdAt,
            this.updatedAt
        );
    }

    public static FcmTokenDocument from(FcmToken fcmToken) {
        return FcmTokenDocument.builder()
            .fcmToken(fcmToken.getFcmToken())
            .subscribedTopics(fcmToken.getSubscribedTopics())
            .deviceType(fcmToken.getDeviceType())
            .isActive(fcmToken.isActive())
            .createdAt(fcmToken.getCreatedAt())
            .build();
    }

    public static List<FcmTokenDocument> from(List<FcmToken> fcmTokens) {
        return fcmTokens.stream().map(FcmTokenDocument::from).toList();
    }

}
