package com.fx.crawler.adapter.out.persistence.repository;

import com.fx.global.adapter.out.persistence.document.DailyApiLogStatisticsDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApiLogStatisticsMongoRepository extends MongoRepository<DailyApiLogStatisticsDocument, String> {

}
