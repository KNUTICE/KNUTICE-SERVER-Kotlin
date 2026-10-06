package com.fx.api.adapter.`in`.web.batch.dto

import com.fx.common.domain.batch.BatchJob
import com.fx.common.domain.batch.BatchJobParameter

data class BatchJobResponse(
    val jobName: String,
    val description: String,
    /** 모두 필수다. 파라미터가 없는 Job 은 빈 목록 */
    val parameters: List<ParameterResponse>,
) {

    data class ParameterResponse(
        val name: String,
        val description: String,
        /** 고를 수 있는 값. null 이면 직접 입력한다 */
        val allowedValues: List<String>?,
    ) {

        companion object {

            fun from(parameter: BatchJobParameter): ParameterResponse =
                ParameterResponse(
                    name = parameter.parameterName,
                    description = parameter.description,
                    allowedValues = parameter.allowedValues,
                )

        }

    }

    companion object {

        fun from(job: BatchJob): BatchJobResponse =
            BatchJobResponse(
                jobName = job.jobName,
                description = job.description,
                parameters = job.parameters.map {
                    ParameterResponse.from(it)
                },
            )

        fun from(jobs: List<BatchJob>): List<BatchJobResponse> =
            jobs.map {
                from(it)
            }

    }

}
