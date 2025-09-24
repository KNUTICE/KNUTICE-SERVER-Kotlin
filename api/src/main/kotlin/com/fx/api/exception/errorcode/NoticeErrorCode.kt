package com.fx.api.exception.errorcode

import com.fx.global.api.BaseErrorCode
import org.springframework.http.HttpStatus

enum class NoticeErrorCode(
    override val httpStatus: HttpStatus,
    override val message: String
) : BaseErrorCode {

    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글이 존재하지 않습니다."),
    SUMMARY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글 요약 내용이 존재하지 않습니다.")

}