package com.fx.global.api

import org.springframework.http.HttpStatus

interface ErrorCodeIfs {

    val httpStatus: HttpStatus

    val message: String

}