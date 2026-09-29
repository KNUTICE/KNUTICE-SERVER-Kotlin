package com.fx.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository

/** [BaseEntity] 동작 확인용 테스트 전용 엔티티. */
@Entity
@Table(name = "sample")
class SampleEntity(
    @Column(nullable = false, length = 50, comment = "이름")
    var name: String,
) : BaseEntity() {

    fun rename(name: String) {
        this.name = name
    }

}

interface SampleRepository : JpaRepository<SampleEntity, Long>
