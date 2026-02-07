package com.fx.api.adapter.out.persistence.repository;

import com.fx.global.adapter.out.persistence.document.DailyApiLogStatisticsDocument;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApiLogStatisticsMongoRepository extends MongoRepository<DailyApiLogStatisticsDocument, String> {

    List<DailyApiLogStatisticsDocument> findByStatisticsDateLessThan(LocalDate date, Pageable pageable);

}
