package com.fx.api.exception

import io.github.seob7.BaseErrorCode

/** [message] 에 오류 코드의 기본 메시지 대신 구체적인 이유(예: 기간 초과)를 담을 수 있다. */
class StatisticsException(
    val baseErrorCode: BaseErrorCode,
    override val message: String = baseErrorCode.message,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
