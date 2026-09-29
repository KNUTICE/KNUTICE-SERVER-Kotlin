package com.fx.crawler.adapter.`in`.batch.support

import com.fx.common.application.port.`in`.CatalogQueryUseCase
import org.springframework.batch.core.job.JobExecution
import org.springframework.batch.core.listener.JobExecutionListener
import org.springframework.stereotype.Component

/** Job 을 시작할 때 토픽 · 알림 문구 카탈로그를 새로 읽는다. 관리자가 바꾼 값이 다음 실행부터 반영된다. */
@Component
class CatalogRefreshJobListener(
    private val catalogQueryUseCase: CatalogQueryUseCase,
) : JobExecutionListener {

    override fun beforeJob(jobExecution: JobExecution) {
        catalogQueryUseCase.refresh()
    }

}
