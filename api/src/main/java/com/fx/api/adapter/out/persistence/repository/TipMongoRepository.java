package com.fx.api.adapter.out.persistence.repository;

import com.fx.api.adapter.out.persistence.document.TipDocument;
import com.fx.global.domain.DeviceType;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TipMongoRepository extends MongoRepository<TipDocument, String> {

    List<TipDocument> findAllByDeviceTypeOrderByCreatedAtDesc(DeviceType deviceType);

}
