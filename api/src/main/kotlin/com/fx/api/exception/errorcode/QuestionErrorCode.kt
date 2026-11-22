package com.fx.api.exception.errorcode

import io.github.seob7.BaseErrorCode
import org.springframework.http.HttpStatus

enum class QuestionErrorCode(
    private val httpStatus: HttpStatus,
    private val message: String
) : BaseErrorCode {

    DAILY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "오늘 질문 횟수를 모두 썼습니다. 내일 다시 이용해 주세요."),
    SERVICE_DAILY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "오늘은 질문이 많아 더 받을 수 없습니다. 내일 다시 이용해 주세요."),
    AI_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "질문을 분석하는 AI 호출 한도에 걸렸습니다. 잠시 후 다시 시도해 주세요."),
    AI_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "질문을 분석하는 AI 서버에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요."),
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "질문 기록이 존재하지 않습니다."),
    FINISH_TOKEN_INVALID(HttpStatus.FORBIDDEN, "답변을 저장할 수 있는 시간이 지났거나 올바르지 않은 요청입니다. 다시 질문해 주세요."),
    ALREADY_FINISHED(HttpStatus.CONFLICT, "이미 답변을 저장했거나 근거 공지가 없는 질문입니다."),
    ANSWER_EMPTY(HttpStatus.BAD_REQUEST, "답변이 비어 있습니다."),
    ;

    override fun getHttpStatus(): HttpStatus =
        httpStatus
    override fun getMessage(): String =
        message

}
