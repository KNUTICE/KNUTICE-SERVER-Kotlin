package com.fx.readingroom.adapter.out.persistence.repository;

import com.fx.readingroom.adapter.out.persistence.document.SeatAlertDocument;
import com.fx.readingroom.domain.SeatAlert.SeatAlertStatus;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SeatAlertMongoRepository extends MongoRepository<SeatAlertDocument, String> {

    List<SeatAlertDocument> findByFcmTokenAndStatusOrderByCreatedAtDesc(
        String fcmToken,
        SeatAlertStatus seatAlertStatus
    );

    int deleteByFcmTokenAndId(String fcmToken, String id);

}
