package com.fx.crawler.adapter.out.persistence.repository;

import com.fx.global.adapter.out.persistence.document.FcmTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FcmTokenMongoRepository extends MongoRepository<FcmTokenDocument, String> {

}
