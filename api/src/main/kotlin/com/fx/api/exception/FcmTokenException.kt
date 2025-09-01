package com.fx.api.exception

import com.fx.global.api.ErrorCodeIfs
import java.lang.RuntimeException

class FcmTokenException(
    val errorCodeIfs: ErrorCodeIfs,
    cause: Throwable? = null
) : RuntimeException(errorCodeIfs.message, cause)