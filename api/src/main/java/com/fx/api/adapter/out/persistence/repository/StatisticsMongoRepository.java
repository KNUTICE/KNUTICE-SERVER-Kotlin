package com.fx.api.adapter.out.persistence.repository;

import com.fx.global.adapter.out.persistence.document.DailyStatisticsDocument;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StatisticsMongoRepository extends MongoRepository<DailyStatisticsDocument, LocalDate> {

    List<DailyStatisticsDocument> findByStatisticsDateLessThan(LocalDate date, Pageable pageable);

}
