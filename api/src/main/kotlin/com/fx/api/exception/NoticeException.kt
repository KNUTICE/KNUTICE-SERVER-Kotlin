package com.fx.api.exception

import com.fx.global.api.BaseErrorCode
import java.lang.RuntimeException

class NoticeException(
    val baseErrorCode: BaseErrorCode,
    cause: Throwable? = null
) : RuntimeException(baseErrorCode.message, cause)