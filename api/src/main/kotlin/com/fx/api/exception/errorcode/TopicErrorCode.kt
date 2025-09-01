package com.fx.api.exception.errorcode

import com.fx.global.api.ErrorCodeIfs
import org.springframework.http.HttpStatus

enum class TopicErrorCode(
    override val httpStatus: HttpStatus,
    override val message: String
) : ErrorCodeIfs {

    TOPIC_NOT_FOUND(HttpStatus.NOT_FOUND, "유효하지 않은 구독입니다.")

}