package adapter.out.persistence.document;

import adapter.out.persistence.base.MongoBaseDocument;
import com.fx.crawler.domain.DeviceType;
import com.fx.crawler.domain.FcmToken;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.el.parser.Token;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "fcm_token")
@SuperBuilder
@NoArgsConstructor
public class FcmTokenDocument extends MongoBaseDocument {

    @Id
    private String fcmToken;

    @Builder.Default
    private boolean generalNewsTopic = true;

    @Builder.Default
    private boolean scholarshipNewsTopic = true;

    @Builder.Default
    private boolean eventNewsTopic = true;

    @Builder.Default
    private boolean academicNewsTopic = true;

    @Builder.Default
    private boolean employmentNewsTopic = true;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private DeviceType deviceType = DeviceType.UNKNOWN;

    public FcmToken toDomain() {
        return new FcmToken(
            this.fcmToken,
            this.generalNewsTopic,
            this.scholarshipNewsTopic,
            this.eventNewsTopic,
            this.academicNewsTopic,
            this.employmentNewsTopic,
            this.deviceType,
            this.isActive,
            this.createdAt,
            this.updatedAt
        );
    }

    public static FcmTokenDocument from(FcmToken fcmToken) {
        return FcmTokenDocument.builder()
            .fcmToken(fcmToken.getFcmToken())
            .generalNewsTopic(fcmToken.getGeneralNewsTopic())
            .scholarshipNewsTopic(fcmToken.getScholarshipNewsTopic())
            .eventNewsTopic(fcmToken.getEventNewsTopic())
            .academicNewsTopic(fcmToken.getAcademicNewsTopic())
            .eventNewsTopic(fcmToken.getEventNewsTopic())
            .deviceType(fcmToken.getDeviceType())
            .isActive(fcmToken.isActive())
            .build();
    }

}
