package com.fx.api.application.service.batch

import com.fx.api.application.port.`in`.batch.BatchJobQueryUseCase
import com.fx.common.domain.batch.BatchJob
import org.springframework.stereotype.Service

/** Job 정의는 코드(common 의 [BatchJob])에 있으므로 DB 를 읽지 않는다. 스케줄 · 수동 실행의 검증도 같은 정의를 쓴다. */
@Service
class BatchJobQueryService : BatchJobQueryUseCase {

    override fun getJobs(): List<BatchJob> =
        BatchJob.entries

}
