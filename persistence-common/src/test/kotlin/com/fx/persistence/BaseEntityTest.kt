package com.fx.persistence

import com.fx.persistence.QSampleEntity.Companion.sampleEntity
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.simple.JdbcClient

@DataJpaTest(properties = ["spring.jpa.hibernate.ddl-auto=create-drop"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySqlContainerConfig::class)
class BaseEntityTest @Autowired constructor(
    private val sampleRepository: SampleRepository,
    private val entityManager: EntityManager,
    private val jdbcClient: JdbcClient,
) {

    @Test
    fun `저장하면 TSID 식별자와 생성·수정 시각이 채워진다`() {
        val saved = sampleRepository.saveAndFlush(SampleEntity(name = "first"))

        assertThat(saved.id).isNotNull()
        assertThat(saved.createdAt).isNotNull()
        assertThat(saved.updatedAt).isNotNull()
    }

    @Test
    fun `TSID 는 생성 순서대로 증가한다`() {
        val first = sampleRepository.saveAndFlush(SampleEntity(name = "first"))
        val second = sampleRepository.saveAndFlush(SampleEntity(name = "second"))

        assertThat(second.id!!).isGreaterThan(first.id!!)
    }

    @Test
    fun `변경 감지로 수정하면 수정 시각이 갱신된다`() {
        val saved = sampleRepository.saveAndFlush(SampleEntity(name = "before"))
        val createdAt = saved.createdAt

        saved.rename("after")
        entityManager.flush()

        assertThat(saved.createdAt).isEqualTo(createdAt)
        assertThat(saved.updatedAt).isAfterOrEqualTo(createdAt)
    }

    @Test
    fun `QueryDSL 로 BaseEntity 를 상속한 엔티티를 조회한다`() {
        sampleRepository.saveAndFlush(SampleEntity(name = "target"))
        sampleRepository.saveAndFlush(SampleEntity(name = "other"))

        val found = JPAQueryFactory(entityManager)
            .selectFrom(sampleEntity)
            .where(sampleEntity.name.eq("target"))
            .orderBy(sampleEntity.id.desc())
            .fetchOne()

        assertThat(found?.name).isEqualTo("target")
    }

    @Test
    fun `Flyway 가 Spring Batch 메타데이터 테이블을 만든다`() {
        val batchTables = jdbcClient
            .sql("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name LIKE 'BATCH\\_%'")
            .query(Long::class.java)
            .single()

        assertThat(batchTables).isEqualTo(9L)
    }

}
