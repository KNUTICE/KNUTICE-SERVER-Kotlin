package com.fx.common.exception

import io.github.seob7.BaseErrorCode
import java.lang.RuntimeException

class NotificationException(
    val baseErrorCode: BaseErrorCode,
    cause: Throwable? = null
) : RuntimeException(baseErrorCode.message, cause)