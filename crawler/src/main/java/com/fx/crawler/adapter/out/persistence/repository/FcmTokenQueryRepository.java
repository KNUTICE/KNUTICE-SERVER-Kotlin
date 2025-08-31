package com.fx.crawler.adapter.out.persistence.repository;

import com.fx.crawler.domain.FcmTokenQuery;
import com.fx.global.adapter.out.persistence.document.FcmTokenDocument;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.fx.global.adapter.out.persistence.document.QFcmTokenDocument.fcmTokenDocument;

@Repository
public class FcmTokenQueryRepository extends QuerydslRepositorySupport {

    public FcmTokenQueryRepository(@Qualifier("mongoTemplate") MongoOperations operations) {
        super(operations);
    }

    /**
     * Cursor 기반 오름차순 batch 조회
     * createdAt 기준 정렬
     */
    public List<FcmTokenDocument> findByCreatedAtAndIsActive(FcmTokenQuery queryParam) {
        return from(fcmTokenDocument)
            .where(
                cursorCondition(queryParam.getCreatedAt(), queryParam.getPageable().getSort()),
                isActive(queryParam.isActive()),
                eqSubscribedTopic(queryParam.getSubscribedTopic())
            )
            .orderBy(getOrderSpecifier(queryParam.getPageable().getSort())
                .toArray(OrderSpecifier[]::new))
            .limit(queryParam.getPageable().getPageSize())
            .fetch();
    }

    private BooleanExpression cursorCondition(LocalDateTime createdAt, Sort sort) {
        if (createdAt == null) return null;

        Sort.Order order = sort.stream().findFirst().orElse(Sort.Order.desc("createdAt"));

        return order.isAscending()
            ? fcmTokenDocument.createdAt.gt(createdAt)
            : fcmTokenDocument.createdAt.lt(createdAt);
    }

    private BooleanExpression isActive(boolean isActive) {
        return fcmTokenDocument.isActive.eq(isActive);
    }

    private BooleanExpression eqSubscribedTopic(String topic) {
        return topic != null ? fcmTokenDocument.subscribedTopics.contains(topic) : null;
    }

    private List<OrderSpecifier> getOrderSpecifier(Sort sort) {
        List<OrderSpecifier> orders = new ArrayList<>();
        sort.stream().forEach(order -> {
            PathBuilder orderByExpression = new PathBuilder(FcmTokenDocument.class, "fcmTokenDocument");
            orders.add(new OrderSpecifier<>(order.isDescending() ? Order.DESC : Order.ASC,
                orderByExpression.get(order.getProperty())));
        });
        return orders;
    }

}