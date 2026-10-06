package com.fx.api.exception.errorcode

import io.github.seob7.BaseErrorCode
import org.springframework.http.HttpStatus

enum class BatchErrorCode(
    private val httpStatus: HttpStatus,
    private val message: String
) : BaseErrorCode {

    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "스케줄이 존재하지 않습니다."),
    SCHEDULE_KEY_DUPLICATED(HttpStatus.CONFLICT, "이미 있는 스케줄 키입니다."),
    CRON_INVALID(HttpStatus.BAD_REQUEST, "cron 이 올바르지 않습니다."),
    JOB_NOT_FOUND(HttpStatus.BAD_REQUEST, "등록되지 않은 Job 입니다."),
    JOB_PARAMETERS_INVALID(HttpStatus.BAD_REQUEST, "Job 파라미터가 올바르지 않습니다."),
    RUN_REQUEST_DUPLICATED(HttpStatus.CONFLICT, "같은 실행 요청이 이미 대기 중입니다."),
    EXECUTION_NOT_FOUND(HttpStatus.NOT_FOUND, "실행 기록이 존재하지 않습니다."),
    ;

    override fun getHttpStatus(): HttpStatus =
        httpStatus
    override fun getMessage(): String =
        message

}
