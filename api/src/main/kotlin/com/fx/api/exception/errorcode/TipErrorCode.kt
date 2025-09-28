package com.fx.api.exception.errorcode

import io.github.seob7.BaseErrorCode
import org.springframework.http.HttpStatus

enum class TipErrorCode(
    private val httpStatus: HttpStatus,
    private val message: String
) : BaseErrorCode {

    TIP_NOT_FOUND(HttpStatus.NOT_FOUND, "TIP 정보가 존재하지 않습니다.")
    ;

    override fun getHttpStatus(): HttpStatus = httpStatus
    override fun getMessage(): String = message

}