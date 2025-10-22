package com.fx.global.exception

import io.github.seob7.BaseErrorCode
import java.lang.RuntimeException

class NoticeException(
    val baseErrorCode: BaseErrorCode,
    cause: Throwable? = null
) : RuntimeException(baseErrorCode.message, cause)