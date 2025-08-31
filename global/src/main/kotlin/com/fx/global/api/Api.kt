package com.fx.global.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class Api<T>(
    val success: Boolean,
    val code: Int,
    val message: String?,
    val data: T?
) {

    companion object {
        @JvmStatic
        fun <T> OK(data: T, message: String): ResponseEntity<Api<T>> =
            ResponseEntity
                .status(HttpStatus.OK)
                .body(
                    Api(
                        success = true,
                        code = HttpStatus.OK.value(),
                        message = message,
                        data = data
                    )
                )

        @JvmStatic
        fun <T> OK(data: T): ResponseEntity<Api<T>> =
            ResponseEntity
                .status(HttpStatus.OK)
                .body(
                    Api(
                        success = true,
                        code = HttpStatus.OK.value(),
                        message = null,
                        data = data
                    )
                )

        @JvmStatic
        fun <T> OK(data: T, httpStatus: HttpStatus): ResponseEntity<Api<T>> =
            ResponseEntity
                .status(httpStatus)
                .body(
                    Api(
                        success = true,
                        code = httpStatus.value(),
                        message = null,
                        data = data
                    )
                )

        @JvmStatic
        fun <T> OK(data: T, message: String?, httpStatus: HttpStatus): ResponseEntity<Api<T>> =
            ResponseEntity
                .status(httpStatus)
                .body(
                    Api(
                        success = true,
                        code = httpStatus.value(),
                        message = message,
                        data = data
                    )
                )

        @JvmStatic
        fun ERROR(errorCodeIfs: ErrorCodeIfs): ResponseEntity<Api<ErrorCodeIfs>> =
            ResponseEntity
                .status(errorCodeIfs.httpStatus)
                .body(
                    Api(
                        success = false,
                        code = errorCodeIfs.httpStatus.value(),
                        message = errorCodeIfs.message,
                        data = null
                    )
                )

        @JvmStatic
        fun ERROR(message: String, httpStatus: HttpStatus): ResponseEntity<Api<String>> =
            ResponseEntity
                .status(httpStatus)
                .body(
                    Api(
                        success = false,
                        code = httpStatus.value(),
                        message = message,
                        data = null
                    )
                )
    }
}