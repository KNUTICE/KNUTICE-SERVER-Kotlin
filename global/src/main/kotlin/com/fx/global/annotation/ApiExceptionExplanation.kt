package com.fx.global.annotation

import com.fx.global.api.BaseErrorCode
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiExceptionExplanation(
    val value: KClass<out BaseErrorCode>,
    val constant: String,
    val name: String = "",
    val mediaType: String = "application/json",
    val summary: String = "",
    val description: String = ""
)