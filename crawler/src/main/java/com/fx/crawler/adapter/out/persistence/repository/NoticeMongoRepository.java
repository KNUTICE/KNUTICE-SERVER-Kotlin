package com.fx.crawler.adapter.out.persistence.repository;

import adapter.out.persistence.document.NoticeDocument;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NoticeMongoRepository extends MongoRepository<NoticeDocument, Long> {

    List<NoticeDocument> findByNttIdIn(List<Long> nttIds);

}
