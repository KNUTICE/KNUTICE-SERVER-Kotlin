package com.fx.api.exception.errorcode

import io.github.seob7.BaseErrorCode
import org.springframework.http.HttpStatus

enum class CollegeErrorCode(
    private val httpStatus: HttpStatus,
    private val message: String
) : BaseErrorCode {

    COLLEGE_NOT_FOUND(HttpStatus.NOT_FOUND, "단과대가 존재하지 않습니다."),
    ;

    override fun getHttpStatus(): HttpStatus =
        httpStatus
    override fun getMessage(): String =
        message

}
