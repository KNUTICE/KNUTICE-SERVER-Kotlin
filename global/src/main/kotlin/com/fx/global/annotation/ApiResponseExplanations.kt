package com.fx.global.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiResponseExplanations(
    val errors: Array<ApiExceptionExplanation> = []
)