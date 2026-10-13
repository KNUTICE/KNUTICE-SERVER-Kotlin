package com.fx.api.application.service.apilog

import com.fx.api.application.port.out.apilog.ApiLogPersistencePort
import com.fx.api.domain.ApiLogStatusGroup
import com.fx.persistence.request.PagingRequest
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.domain.Page
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ApiLogQueryServiceTest : BehaviorSpec({

    val zone = ZoneId.of("Asia/Seoul")
    // KST 00:30 은 UTC 로 전날이다
    val clock = Clock.fixed(LocalDateTime.of(2026, 10, 13, 0, 30).atZone(zone).toInstant(), zone)
    val today = LocalDate.of(2026, 10, 13)
    val pageable = PagingRequest().toPageable()

    val persistencePort = mockk<ApiLogPersistencePort>()
    val service = ApiLogQueryService(persistencePort, clock)

    every {
        persistencePort.findApiLogs(any(), any(), any(), any())
    } returns Page.empty()

    Given("날짜를 주지 않으면") {
        service.getApiLogs(null, null, null, pageable)

        Then("오늘(KST) 로그를 조회한다") {
            verify {
                persistencePort.findApiLogs(today, null, null, pageable)
            }
        }
    }

    Given("날짜와 조건을 주면") {
        val date = LocalDate.of(2026, 10, 12)
        service.getApiLogs(date, "/open-api/v1/notices", ApiLogStatusGroup.SERVER_ERROR, pageable)

        Then("그대로 조회한다") {
            verify {
                persistencePort.findApiLogs(date, "/open-api/v1/notices", ApiLogStatusGroup.SERVER_ERROR, pageable)
            }
        }
    }

})
