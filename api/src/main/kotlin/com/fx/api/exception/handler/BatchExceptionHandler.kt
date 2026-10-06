package com.fx.api.exception.handler

import com.fx.api.exception.BatchException
import io.github.seob7.Api
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class BatchExceptionHandler {

    @ExceptionHandler(BatchException::class)
    fun handleBatchException(e: BatchException): ResponseEntity<Api<String>> =
        Api.ERROR(e.message, e.baseErrorCode.httpStatus)

}
