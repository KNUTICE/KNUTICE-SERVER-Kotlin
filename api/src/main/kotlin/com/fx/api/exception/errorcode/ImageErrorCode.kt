package com.fx.api.exception.errorcode

import com.fx.global.api.BaseErrorCode
import org.springframework.http.HttpStatus

enum class ImageErrorCode(
    override val httpStatus: HttpStatus,
    override val message: String
) : BaseErrorCode {

    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지가 존재하지 않습니다.")

}