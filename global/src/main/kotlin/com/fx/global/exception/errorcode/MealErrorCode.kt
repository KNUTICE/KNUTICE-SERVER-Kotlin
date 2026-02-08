package com.fx.global.exception.errorcode

import io.github.seob7.BaseErrorCode
import org.springframework.http.HttpStatus

enum class MealErrorCode(
    private val httpStatus: HttpStatus,
    private val message: String
) : BaseErrorCode {

    MEAL_NOT_FOUND(HttpStatus.NOT_FOUND, "학식 정보를 조회할 수 없습니다.")
    ;

    override fun getHttpStatus(): HttpStatus = httpStatus
    override fun getMessage(): String = message

}