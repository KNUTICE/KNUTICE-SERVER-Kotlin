package com.fx.api.exception.errorcode

import io.github.seob7.BaseErrorCode
import org.springframework.http.HttpStatus

enum class QuestionErrorCode(
    private val httpStatus: HttpStatus,
    private val message: String
) : BaseErrorCode {

    DAILY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "오늘 질문 한도를 모두 썼습니다. 한국시간 17시에 다시 열립니다."),
    AI_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "AI 호출 한도에 걸렸습니다. 잠시 후 다시 시도해 주세요."),
    AI_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI 서버에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요."),
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "질문 기록이 존재하지 않습니다."),
    ;

    override fun getHttpStatus(): HttpStatus =
        httpStatus
    override fun getMessage(): String =
        message

}
