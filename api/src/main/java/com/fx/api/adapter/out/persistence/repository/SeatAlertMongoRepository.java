package com.fx.api.adapter.out.persistence.repository;

import com.fx.global.adapter.out.persistence.document.SeatAlertDocument;
import com.fx.global.domain.readingroom.SeatAlert.SeatAlertStatus;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SeatAlertMongoRepository extends MongoRepository<SeatAlertDocument, String> {

    List<SeatAlertDocument> findByFcmTokenAndStatus(String fcmToken, SeatAlertStatus seatAlertStatus);

}
