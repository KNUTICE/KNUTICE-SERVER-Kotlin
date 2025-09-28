package com.fx.api.exception.errorcode

import io.github.seob7.BaseErrorCode
import org.springframework.http.HttpStatus

enum class TopicErrorCode(
    private val httpStatus: HttpStatus,
    private val message: String
) : BaseErrorCode {

    TOPIC_NOT_FOUND(HttpStatus.NOT_FOUND, "유효하지 않은 구독입니다.")
    ;

    override fun getHttpStatus(): HttpStatus = httpStatus
    override fun getMessage(): String = message

}