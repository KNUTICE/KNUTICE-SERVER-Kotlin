package adapter.out.persistence.repository;

import adapter.out.persistence.document.FcmTokenDocument;
import com.fx.crawler.domain.FcmTokenQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class FcmTokenQueryRepository {

    private final MongoTemplate mongoTemplate;

    /**
     * Cursor 기반 batch 조회
     * createdAt 기준 ASC 정렬
     */
    public List<FcmTokenDocument> findByCreatedAtAndIsActive(FcmTokenQuery fcmTokenQuery) {
        Query query = new Query();

        Sort sort = fcmTokenQuery.getPageable().getSort();
        if (sort.isUnsorted()) {
            sort = Sort.by(Sort.Direction.ASC, "createdAt");
        }

        // cursor 조건
        if (fcmTokenQuery.getCreatedAt() != null) {
            Sort.Order order = sort.stream().findFirst().orElse(Sort.Order.asc("createdAt"));
            if (order.isAscending()) {
                query.addCriteria(Criteria.where("createdAt").gt(fcmTokenQuery.getCreatedAt()));
            } else {
                query.addCriteria(Criteria.where("createdAt").lt(fcmTokenQuery.getCreatedAt()));
            }
        }

        // isActive 조건
        query.addCriteria(Criteria.where("isActive").is(fcmTokenQuery.isActive()));

        // topic 조건
        if (fcmTokenQuery.getSubscribedTopic() != null) {
            query.addCriteria(Criteria.where("subscribedTopics").is(fcmTokenQuery.getSubscribedTopic()));
        }

        query.limit(fcmTokenQuery.getPageable().getPageSize());
        query.with(sort);

        return mongoTemplate.find(query, FcmTokenDocument.class);
    }

}