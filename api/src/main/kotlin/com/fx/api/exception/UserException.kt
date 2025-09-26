package com.fx.api.exception

import io.github.seob7.BaseErrorCode
import java.lang.RuntimeException

class UserException(
    val baseErrorCode: BaseErrorCode,
    cause: Throwable? = null
) : RuntimeException(baseErrorCode.message, cause)