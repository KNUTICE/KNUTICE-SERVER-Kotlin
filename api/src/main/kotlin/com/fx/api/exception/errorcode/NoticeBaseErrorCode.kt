package com.fx.api.exception.errorcode

import com.fx.global.api.BaseErrorCode
import org.springframework.http.HttpStatus

enum class NoticeBaseErrorCode(
    override val httpStatus: HttpStatus,
    override val message: String
) : BaseErrorCode {

    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글이 존재하지 않습니다.")


}