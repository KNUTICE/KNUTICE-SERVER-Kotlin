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
@Import(PostgreSqlContainerConfig::class)
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
    fun `Flyway 가 Spring Batch 메타데이터 테이블과 시퀀스를 만든다`() {
        // 따옴표 없는 이름은 소문자로 저장된다. batch_schedule 같은 업무 테이블과 구분하려고 job · step 접두어로 거른다
        val batchTables = jdbcClient
            .sql(
                """
                SELECT table_name FROM information_schema.tables
                WHERE table_schema = current_schema()
                  AND (table_name LIKE 'batch\_job\_%' OR table_name LIKE 'batch\_step\_%')
                """
            )
            .query(String::class.java)
            .list()
        val batchSequences = jdbcClient
            .sql("SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema = current_schema()")
            .query(String::class.java)
            .list()

        assertThat(batchTables).containsExactlyInAnyOrder(
            "batch_job_instance",
            "batch_job_execution",
            "batch_job_execution_params",
            "batch_job_execution_context",
            "batch_step_execution",
            "batch_step_execution_context",
        )
        assertThat(batchSequences).containsExactlyInAnyOrder(
            "batch_job_instance_seq",
            "batch_job_execution_seq",
            "batch_step_execution_seq",
        )
    }

    @Test
    fun `운영 DB 처럼 pgvector 확장이 만들어져 있다`() {
        val vectorExtensions = jdbcClient
            .sql("SELECT COUNT(*) FROM pg_extension WHERE extname = 'vector'")
            .query(Long::class.java)
            .single()

        assertThat(vectorExtensions).isEqualTo(1L)
    }

}
