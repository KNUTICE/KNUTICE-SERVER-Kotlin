package com.fx.api.exception

import io.github.seob7.BaseErrorCode


class JwtException (
    val baseErrorCode: BaseErrorCode,
    cause: Throwable? = null
) : RuntimeException(baseErrorCode.message, cause)