package com.fx.crawler.adapter.out.persistence.repository;

import com.fx.common.adapter.out.persistence.document.DailyApiLogStatisticsDocument
import org.springframework.data.mongodb.repository.MongoRepository


interface ApiLogStatisticsMongoRepository : MongoRepository<DailyApiLogStatisticsDocument, String> {

}
