package com.fx.api.adapter.out.persistence.repository;

import com.fx.global.adapter.out.persistence.document.SeatAlertDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SeatAlertMongoRepository extends MongoRepository<SeatAlertDocument, String> {

}
