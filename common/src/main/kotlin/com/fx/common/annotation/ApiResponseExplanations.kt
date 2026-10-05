package com.fx.common.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiResponseExplanations(
    val errors: Array<ApiExceptionExplanation> = []
)