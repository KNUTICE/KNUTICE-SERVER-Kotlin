package com.fx.crawler.adapter.out.persistence.repository;

import com.fx.global.adapter.out.persistence.document.NoticeDocument;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface NoticeMongoRepository extends MongoRepository<NoticeDocument, Long> {

    List<NoticeDocument> findByNttIdIn(List<Long> nttIds);

    // content가 존재하고(null이 아님), contentSummary가 없는(null) 데이터를 최신순으로 페이징하여 조회
    @Query("{ 'content': { $exists: true, $ne: '' }, 'contentSummary': { $exists: false } }")
    List<NoticeDocument> findByContentSummaryIsNullOrderByCreatedAtDesc(Pageable pageable);
}
