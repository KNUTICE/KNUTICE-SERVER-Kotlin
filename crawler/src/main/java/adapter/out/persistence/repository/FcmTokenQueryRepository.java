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

        // cursor 조건
        if (fcmTokenQuery.getCreatedAt() != null) {
            // ASC 기준이면 cursor 이후 데이터 조회
            Sort.Order order = fcmTokenQuery.getPageable().getSort().stream()
                .findFirst()
                .orElse(Sort.Order.asc("createdAt"));

            if (order.isAscending()) {
                query.addCriteria(Criteria.where("createdAt").gt(fcmTokenQuery.getCreatedAt()));
            } else {
                query.addCriteria(Criteria.where("createdAt").lt(fcmTokenQuery.getCreatedAt()));
            }
        }

        // isActive 조건
        query.addCriteria(Criteria.where("isActive").is(fcmTokenQuery.isActive()));

        if (fcmTokenQuery.getSubscribedTopic() != null) {
            query.addCriteria(Criteria.where("subscribedTopics").is(fcmTokenQuery.getSubscribedTopic()));
        }

        // 페이징 + 정렬
        query.with(fcmTokenQuery.getPageable());

        return mongoTemplate.find(query, FcmTokenDocument.class);
    }
}
