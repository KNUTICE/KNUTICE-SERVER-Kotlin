package com.fx.api.exception

import com.fx.global.api.ErrorCodeIfs
import java.lang.RuntimeException

class TipException(
    val errorCodeIfs: ErrorCodeIfs,
    cause: Throwable? = null
) : RuntimeException(errorCodeIfs.message, cause)