package com.fx.persistence

import io.hypersistence.utils.hibernate.id.Tsid
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

/**
 * 모든 JPA Entity 가 상속하는 공통 영속성 메타데이터.
 *
 * - 식별자는 DB auto increment 가 아닌 TSID 로 애플리케이션에서 생성한다.
 *   생성 순서대로 증가하므로 id 정렬이 곧 생성순이고, IDENTITY 와 달리 JDBC 배치 INSERT 가 동작한다.
 * - 생성·수정 시각은 JPA Auditing 으로 관리하며, 실행 애플리케이션에 `@EnableJpaAuditing` 이 필요하다.
 * - 논리 삭제가 필요한 엔티티는 각자 `deletedAt` 을 둔다.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {

    @Id
    @Tsid
    var id: Long? = null
        protected set

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, comment = "생성 시각")
    var createdAt: LocalDateTime? = null
        protected set

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, comment = "수정 시각")
    var updatedAt: LocalDateTime? = null
        protected set

}
