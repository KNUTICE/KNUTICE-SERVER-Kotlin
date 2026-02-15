package com.fx.global.adapter.out.persistence.document;

import com.fx.global.adapter.out.persistence.base.MongoBaseDocument;
import com.fx.global.domain.readingroom.SeatAlert;
import com.fx.global.domain.readingroom.SeatAlert.ReadingRoom;
import com.fx.global.domain.readingroom.SeatAlert.SeatAlertStatus;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "seat_alert")
@SuperBuilder
@NoArgsConstructor
public class SeatAlertDocument extends MongoBaseDocument {

    @Id
    private String id;

    private String fcmToken;

    private ReadingRoom readingRoom;

    private String seatNumber;

    private SeatAlertStatus status;

    private LocalDateTime notifiedAt;

    private LocalDateTime expiresAt;

    public static SeatAlertDocument from(SeatAlert seatAlert) {
        return SeatAlertDocument.builder()
            .id(seatAlert.getId())
            .fcmToken(seatAlert.getFcmToken())
            .readingRoom(seatAlert.getReadingRoom())
            .seatNumber(seatAlert.getSeatNumber())
            .status(seatAlert.getStatus())
            .notifiedAt(seatAlert.getNotifiedAt())
            .expiresAt(seatAlert.getExpiresAt())
            .createdAt(seatAlert.getCreatedAt())
            .build();
    }

    public SeatAlert toDomain() {
        return new SeatAlert(
            this.id,
            this.fcmToken,
            this.readingRoom,
            this.seatNumber,
            this.status,
            this.notifiedAt,
            this.expiresAt,
            this.createdAt,
            this.updatedAt
        );
    }
}