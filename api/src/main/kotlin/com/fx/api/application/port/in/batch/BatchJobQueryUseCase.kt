package com.fx.api.application.port.`in`.batch

import com.fx.common.domain.batch.BatchJob

interface BatchJobQueryUseCase {

    /** crawler 가 실행하는 Job 과 Job 별 파라미터 */
    fun getJobs(): List<BatchJob>

}
