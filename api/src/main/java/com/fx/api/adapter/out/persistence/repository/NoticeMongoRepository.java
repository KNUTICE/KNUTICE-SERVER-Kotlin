package com.fx.api.adapter.out.persistence.repository;

import com.fx.global.adapter.out.persistence.document.NoticeDocument;
import java.time.LocalDateTime;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NoticeMongoRepository extends MongoRepository<NoticeDocument, Long> {

    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Long countByCreatedAtBetweenAndContentSummaryIsNotNull(LocalDateTime start, LocalDateTime end);

}
