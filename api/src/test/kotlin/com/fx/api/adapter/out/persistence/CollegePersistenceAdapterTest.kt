package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.CollegeQueryRepository
import com.fx.api.config.querydsl.QuerydslConfig
import com.fx.common.adapter.out.persistence.repository.CollegeRepository
import com.fx.common.adapter.out.persistence.repository.TopicRepository
import com.fx.persistence.MySqlContainerConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import java.time.LocalDateTime

/** 실제 MySQL 에서 Flyway 로 시드된 단과대 · 토픽을 조회한다. */
@DataJpaTest(properties = ["MYSQL_URL=unused", "MYSQL_DATABASE=unused", "MYSQL_USERNAME=unused", "MYSQL_PASSWORD=unused"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySqlContainerConfig::class, QuerydslConfig::class, CollegeQueryRepository::class, CollegePersistenceAdapter::class)
class CollegePersistenceAdapterTest @Autowired constructor(
    private val collegePersistenceAdapter: CollegePersistenceAdapter,
    private val collegeRepository: CollegeRepository,
    private val topicRepository: TopicRepository,
) {

    private val now = LocalDateTime.of(2026, 10, 22, 12, 0)

    @Test
    fun `삭제되지 않은 단과대를 표시 순서대로 조회한다`() {
        collegeRepository.findAll().forEach {
            when (it.collegeKey) {
                "DEPRECATED" -> it.changeDisplayOrder(100)
                "FUTURE_CONVERGENCE" -> it.delete(now)
            }
        }
        collegeRepository.flush()

        val collegeKeys = collegePersistenceAdapter.findAll().map {
            it.collegeKey
        }

        assertThat(collegeKeys).containsExactly(
            "ENGINEERING",
            "TRANSPORTATION_ENGINEERING",
            "AI_CONVERGENCE",
            "HUMANITIES",
            "SOCIAL_SCIENCES",
            "HEALTH_AND_LIFE_SCIENCE",
            "RAILROAD_SCIENCES",
            "DEPRECATED",
        )
    }

    @Test
    fun `단과대별로 삭제되지 않은 소속 학과 수를 센다`() {
        val collegeIds = collegeRepository.findAll().associate {
            it.collegeKey to requireNotNull(it.id)
        }
        val engineeringId = requireNotNull(collegeIds["ENGINEERING"])
        topicRepository.findAll().first {
            it.collegeId == engineeringId
        }.delete(now)
        topicRepository.flush()

        val counts = collegePersistenceAdapter.countTopicsByCollegeId()

        assertThat(counts[engineeringId]).isEqualTo(13L)
        assertThat(counts[collegeIds["DEPRECATED"]]).isEqualTo(3L)
        assertThat(counts.keys).containsExactlyInAnyOrderElementsOf(collegeIds.values)
    }

}
