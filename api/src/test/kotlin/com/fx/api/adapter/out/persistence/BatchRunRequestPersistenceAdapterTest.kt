package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.BatchRunRequestQueryRepository
import com.fx.api.config.querydsl.QuerydslConfig
import com.fx.common.adapter.out.persistence.repository.BatchRunRequestRepository
import com.fx.common.domain.batch.BatchRunRequest
import com.fx.common.domain.batch.BatchRunRequestStatus
import com.fx.persistence.MySqlContainerConfig
import com.fx.persistence.request.PagingRequest
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import java.time.LocalDateTime

/** 실제 MySQL 에서 수동 실행 요청을 저장 · 조회한다. */
@DataJpaTest(properties = ["MYSQL_URL=unused", "MYSQL_DATABASE=unused", "MYSQL_USERNAME=unused", "MYSQL_PASSWORD=unused"])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(MySqlContainerConfig::class, QuerydslConfig::class, BatchRunRequestQueryRepository::class, BatchRunRequestPersistenceAdapter::class)
class BatchRunRequestPersistenceAdapterTest @Autowired constructor(
    private val batchRunRequestPersistenceAdapter: BatchRunRequestPersistenceAdapter,
    private val batchRunRequestRepository: BatchRunRequestRepository,
    private val entityManager: EntityManager,
) {

    private val now = LocalDateTime.of(2026, 10, 6, 14, 0)

    private lateinit var ids: List<Long>

    /** 오래된 순으로 공지 크롤링(대기) · 사일런트 푸시(실행) · 학과 크롤링(대기) · 정리(거절) */
    @BeforeEach
    fun setUp() {
        val requests = listOf(
            BatchRunRequest("noticeCrawlJob", """{"topicType":"NOTICE"}""", "관리자"),
            BatchRunRequest("silentPushJob", "{}", "관리자"),
            BatchRunRequest("noticeCrawlJob", """{"topicType":"MAJOR"}""", "관리자"),
            BatchRunRequest("maintenanceJob", """{"retentionDays":"7"}""", "관리자"),
        ).map {
            requireNotNull(batchRunRequestPersistenceAdapter.save(it).id)
        }
        entityManager.flush()
        batchRunRequestRepository.claim(requests[1], now)
        batchRunRequestRepository.reject(requests[3], "같은 작업이 이미 실행 중입니다.", now)
        entityManager.clear()
        ids = requests
    }

    @Test
    fun `최신순으로 페이지 단위 조회한다`() {
        val first = batchRunRequestPersistenceAdapter.findRequests(status = null, PagingRequest(page = 1, size = 3).toPageable())
        assertThat(idsOf(first.content)).containsExactly(ids[3], ids[2], ids[1])
        assertThat(first.totalElements).isEqualTo(4)
        assertThat(first.hasNext()).isTrue()

        val second = batchRunRequestPersistenceAdapter.findRequests(status = null, PagingRequest(page = 2, size = 3).toPageable())
        assertThat(idsOf(second.content)).containsExactly(ids[0])
        assertThat(second.hasNext()).isFalse()
    }

    @Test
    fun `상태로 거를 수 있다`() {
        val requested = batchRunRequestPersistenceAdapter.findRequests(BatchRunRequestStatus.REQUESTED, PagingRequest().toPageable())
        assertThat(idsOf(requested.content)).containsExactly(ids[2], ids[0])
        assertThat(requested.totalElements).isEqualTo(2)

        val rejected = batchRunRequestPersistenceAdapter.findRequests(BatchRunRequestStatus.REJECTED, PagingRequest().toPageable()).content.single()
        assertThat(rejected.rejectReason).isEqualTo("같은 작업이 이미 실행 중입니다.")
        assertThat(rejected.createdAt).isNotNull()
    }

    @Test
    fun `같은 Job 의 대기 중인 요청만 찾는다`() {
        assertThat(idsOf(batchRunRequestPersistenceAdapter.findRequested("noticeCrawlJob"))).containsExactlyInAnyOrder(ids[0], ids[2])
        assertThat(batchRunRequestPersistenceAdapter.findRequested("silentPushJob")).isEmpty()
    }

    private fun idsOf(requests: List<BatchRunRequest>): List<Long?> =
        requests.map {
            it.id
        }

}
