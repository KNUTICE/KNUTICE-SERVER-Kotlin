package com.fx.api.exception.errorcode

import com.fx.global.api.ErrorCodeIfs
import org.springframework.http.HttpStatus

enum class TipErrorCode(
    override val httpStatus: HttpStatus,
    override val message: String
) : ErrorCodeIfs {

    TIP_NOT_FOUND(HttpStatus.NOT_FOUND, "TIP 정보가 존재하지 않습니다.")

}