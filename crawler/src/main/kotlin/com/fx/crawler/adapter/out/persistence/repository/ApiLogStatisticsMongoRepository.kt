package com.fx.crawler.adapter.out.persistence.repository;

import com.fx.global.adapter.out.persistence.document.DailyApiLogStatisticsDocument
import org.springframework.data.mongodb.repository.MongoRepository


interface ApiLogStatisticsMongoRepository : MongoRepository<DailyApiLogStatisticsDocument, String> {

}
