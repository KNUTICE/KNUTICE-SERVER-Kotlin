package com.fx.api.exception

import io.github.seob7.BaseErrorCode

class AuthException(
    val baseErrorCode: BaseErrorCode,
    cause: Throwable? = null
) : RuntimeException(baseErrorCode.message, cause)
