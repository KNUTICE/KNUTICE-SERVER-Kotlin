package com.fx.api.exception

import com.fx.global.api.BaseErrorCode
import java.lang.RuntimeException

class TopicException(
    val baseErrorCode: BaseErrorCode,
    cause: Throwable? = null
) : RuntimeException(baseErrorCode.message, cause)