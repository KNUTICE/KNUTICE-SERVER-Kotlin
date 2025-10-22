package com.fx.global.exception.errorcode

import io.github.seob7.BaseErrorCode
import org.springframework.http.HttpStatus

enum class NoticeErrorCode(
    private val httpStatus: HttpStatus,
    private val message: String
) : BaseErrorCode {

    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글이 존재하지 않습니다."),
    SUMMARY_CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글 요약 내용이 존재하지 않습니다."),
    ALREADY_EXISTS(HttpStatus.CONFLICT, "게시글이 이미 존재합니다.");
    ;

    override fun getHttpStatus(): HttpStatus = httpStatus
    override fun getMessage(): String = message

}