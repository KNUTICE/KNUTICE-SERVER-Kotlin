package com.fx.api.application.service.batch

import com.fx.api.exception.BatchException
import com.fx.api.exception.errorcode.BatchErrorCode
import com.fx.common.domain.batch.BatchCron
import com.fx.common.domain.batch.BatchJob
import com.fx.common.domain.batch.BatchJobParameters
import java.time.LocalDateTime

/**
 * 관리자가 입력한 Job · 파라미터 · cron 을 저장하기 전에 검증한다.
 * crawler 도 실행하기 전에 같은 규칙으로 검증하지만, 잘못된 값이 1분 뒤 거절되기 전에 바로 알려 준다.
 */
object BatchInputValidator {

    /** @return 저장할 Job 파라미터 JSON */
    fun jobParameters(jobName: String, parameters: Map<String, String>): String {
        val job = BatchJob.from(jobName)
            ?: throw BatchException(BatchErrorCode.JOB_NOT_FOUND, "등록되지 않은 Job 입니다: $jobName")
        try {
            job.validate(parameters)
        } catch (e: IllegalArgumentException) {
            throw BatchException(BatchErrorCode.JOB_PARAMETERS_INVALID, e.message ?: BatchErrorCode.JOB_PARAMETERS_INVALID.message, e)
        }
        return BatchJobParameters.format(parameters)
    }

    /**
     * 다음 실행 시각이 없는 cron(예: 2월 30일)도 거절한다.
     * @return 앞뒤 공백을 뺀 cron
     */
    fun cron(expression: String, now: LocalDateTime): String {
        val cron = try {
            BatchCron.parse(expression).also {
                it.next(now)
            }
        } catch (e: IllegalArgumentException) {
            throw invalidCron(e)
        } catch (e: IllegalStateException) {
            throw invalidCron(e)
        }
        return cron.expression
    }

    private fun invalidCron(e: RuntimeException): BatchException =
        BatchException(BatchErrorCode.CRON_INVALID, e.message ?: BatchErrorCode.CRON_INVALID.message, e)

}
