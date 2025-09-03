package com.fx.api.exception

import com.fx.global.api.BaseErrorCode

class JwtException (
    val baseErrorCode: BaseErrorCode,
    cause: Throwable? = null
) : RuntimeException(baseErrorCode.message, cause)