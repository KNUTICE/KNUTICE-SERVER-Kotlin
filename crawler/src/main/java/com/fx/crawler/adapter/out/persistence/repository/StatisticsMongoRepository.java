package com.fx.crawler.adapter.out.persistence.repository;

import com.fx.global.adapter.out.persistence.document.DailyStatisticsDocument;
import java.time.LocalDate;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StatisticsMongoRepository extends MongoRepository<DailyStatisticsDocument, LocalDate> {

}
