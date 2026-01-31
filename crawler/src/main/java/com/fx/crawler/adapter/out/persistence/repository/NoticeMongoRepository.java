package com.fx.crawler.adapter.out.persistence.repository;

import com.fx.global.adapter.out.persistence.document.NoticeDocument;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NoticeMongoRepository extends MongoRepository<NoticeDocument, Long> {

    List<NoticeDocument> findByNttIdIn(List<Long> nttIds);

    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Long countByCreatedAtBetweenAndContentSummaryIsNotNull(LocalDateTime start, LocalDateTime end);

}
