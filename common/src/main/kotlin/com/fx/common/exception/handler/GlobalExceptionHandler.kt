package com.fx.common.exception.handler

import io.github.seob7.Api
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import tools.jackson.databind.DatabindException
import kotlin.collections.joinToString
import kotlin.jvm.java

/**
 * 요청 값 오류를 공통 400 응답으로 바꾼다.
 *
 * 여기서 잡지 않은 예외는 `/error` 로 넘어가는데, 보안 설정이 이 경로를 막아 본문 없는 403 이 된다.
 * 그러면 잘못된 요청이 토큰 만료와 구분되지 않으므로 요청 본문 · 경로 · 쿼리 파라미터 오류는 모두 여기서 잡는다.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<Api<String>> {
        val errorMessage = e.bindingResult.fieldErrors.joinToString(" ") { fieldError ->
            "[${fieldError.field}](은)는 ${fieldError.defaultMessage} 입력된 값: [${fieldError.rejectedValue}]"
        }
        log.error("", e)
        return Api.ERROR(errorMessage, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(e: ConstraintViolationException): ResponseEntity<Api<String>> {
        val errorMessage = e.constraintViolations.joinToString(" ") { violation ->
            val field = violation.propertyPath.toString()
            val msg = violation.message
            "[${field}] ${msg} (입력된 값: ${violation.invalidValue})"
        }
        log.error("Validation error: {}", errorMessage, e)
        return Api.ERROR(errorMessage, HttpStatus.BAD_REQUEST)
    }

    /** 요청 본문을 읽지 못했다. JSON 문법 오류, 필수 필드 누락, 필드 타입 불일치 */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<Api<String>> {
        val field = (e.cause as? DatabindException)?.path?.joinToString(".") {
            it.propertyName ?: "[${it.index}]"
        }
        val errorMessage = if (field.isNullOrEmpty()) {
            "요청 본문이 없거나 올바른 JSON 형식이 아닙니다."
        } else {
            "[$field](은)는 값이 없거나 형식이 올바르지 않습니다."
        }
        return badRequest(errorMessage, e)
    }

    /** 경로 변수 · 쿼리 파라미터를 지정한 타입으로 바꾸지 못했다 (예: 숫자 자리에 문자, 없는 enum 값) */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<Api<String>> {
        val allowedValues = e.requiredType?.enumConstants?.joinToString(", ")
        val reason = if (allowedValues != null) "$allowedValues 중 하나여야 합니다." else "형식이 올바르지 않습니다."
        return badRequest("[${e.name}](은)는 $reason 입력된 값: [${e.value}]", e)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(e: MissingServletRequestParameterException): ResponseEntity<Api<String>> =
        badRequest("[${e.parameterName}](은)는 필수입니다.", e)

    /** 클라이언트 입력 오류라 스택 없이 남긴다. */
    private fun badRequest(errorMessage: String, e: Exception): ResponseEntity<Api<String>> {
        log.warn("잘못된 요청: {} ({})", errorMessage, e.message)
        return Api.ERROR(errorMessage, HttpStatus.BAD_REQUEST)
    }

}
