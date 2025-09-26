package com.fx.global.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

data class MetaData(
    val success: Boolean,
    val code: Int,
    val message: String?
)

class Api<T>(
    val metaData: MetaData,
    val data: T?
) {

    companion object {
        @JvmStatic
        fun <T> OK(data: T, message: String): ResponseEntity<Api<T>> =
            ResponseEntity
                .status(HttpStatus.OK)
                .body(
                    Api(
                        metaData = MetaData(
                            success = true,
                            code = HttpStatus.OK.value(),
                            message = message,
                        ),
                        data = data
                    )
                )

        @JvmStatic
        fun <T> OK(data: T): ResponseEntity<Api<T>> =
            ResponseEntity
                .status(HttpStatus.OK)
                .body(
                    Api(
                        metaData = MetaData(
                            success = true,
                            code = HttpStatus.OK.value(),
                            message = null,
                        ),
                        data = data
                    )
                )

        @JvmStatic
        fun <T> OK(data: T, httpStatus: HttpStatus): ResponseEntity<Api<T>> =
            ResponseEntity
                .status(httpStatus)
                .body(
                    Api(
                        metaData = MetaData(
                            success = true,
                            code = httpStatus.value(),
                            message = null,
                        ),
                        data = data
                    )
                )

        @JvmStatic
        fun <T> OK(data: T, message: String?, httpStatus: HttpStatus): ResponseEntity<Api<T>> =
            ResponseEntity
                .status(httpStatus)
                .body(
                    Api(
                        metaData = MetaData(
                            success = true,
                            code = httpStatus.value(),
                            message = message,
                        ),
                        data = data
                    )
                )

        @JvmStatic
        fun ERROR(baseErrorCode: BaseErrorCode): ResponseEntity<Api<BaseErrorCode>> =
            ResponseEntity
                .status(baseErrorCode.httpStatus)
                .body(
                    Api(
                        metaData = MetaData(
                            success = false,
                            code = baseErrorCode.httpStatus.value(),
                            message = baseErrorCode.message,
                        ),
                        data = null
                    )
                )

        @JvmStatic
        fun ERROR(message: String, httpStatus: HttpStatus): ResponseEntity<Api<String>> =
            ResponseEntity
                .status(httpStatus)
                .body(
                    Api(
                        metaData = MetaData(
                            success = false,
                            code = httpStatus.value(),
                            message = message,
                        ),
                        data = null
                    )
                )
    }
}