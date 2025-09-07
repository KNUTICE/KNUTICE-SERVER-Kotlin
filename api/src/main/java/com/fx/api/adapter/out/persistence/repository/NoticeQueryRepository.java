package com.fx.api.adapter.out.persistence.repository;

import com.fx.api.domain.NoticeQuery;
import com.fx.global.adapter.out.persistence.document.NoticeDocument;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import static com.fx.global.adapter.out.persistence.document.QNoticeDocument.noticeDocument;

@Repository
public class NoticeQueryRepository extends QuerydslRepositorySupport {

    public NoticeQueryRepository(@Qualifier("mongoTemplate") MongoOperations operations) {
        super(operations);
    }

    /**
     * Cursor 기반 조회
     * nttId 기준 DESC 정렬
     */
    public List<NoticeDocument> findByNotice(NoticeQuery noticeQuery) {
        return from(noticeDocument)
            .where(
                eqNoticeType(noticeQuery),
                containKeyword(noticeQuery.getKeyword()),
                ltNttId(noticeQuery.getNttId())
            )
            .orderBy(getOrderSpecifier(noticeQuery.getPageable().getSort())
                .toArray(OrderSpecifier[]::new))
            .limit(noticeQuery.getPageable().getPageSize())
            .fetch();
    }

    private BooleanExpression eqNoticeType(NoticeQuery noticeQuery) {
        return noticeQuery.getTopic() != null
            ? noticeDocument.topic.eq(noticeQuery.getTopic().getTopicName())
            : null;
    }

    private BooleanExpression containKeyword(String keyword) {
        return keyword != null && !keyword.isBlank()
            ? noticeDocument.title.contains(keyword)
            : null;
    }

    private Predicate ltNttId(Long nttId) {
        return nttId != null ? noticeDocument.nttId.lt(nttId) : null;
    }

    private List<OrderSpecifier> getOrderSpecifier(Sort sort) {
        List<OrderSpecifier> orders = new ArrayList<>();
        sort.stream().forEach(order -> {
            PathBuilder orderByExpression = new PathBuilder(NoticeDocument.class,
                "noticeDocument");
            orders.add(new OrderSpecifier<>(order.isDescending() ? Order.DESC : Order.ASC,
                orderByExpression.get(order.getProperty())));

        });
        return orders;
    }

}