package com.fx.api.config.swagger

import com.fx.global.annotation.ApiExceptionExplanation
import com.fx.global.annotation.ApiResponseExplanations
import com.fx.global.api.Api
import com.fx.global.api.BaseErrorCode
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import org.springframework.web.method.HandlerMethod
import kotlin.reflect.KClass

object ApiExceptionExplainParser {

    fun parse(operation: Operation, handlerMethod: HandlerMethod) {
        val annotation = handlerMethod.getMethodAnnotation(ApiResponseExplanations::class.java)
        if (annotation != null) {
            generateExceptionResponseDocs(operation, annotation.errors)
        }
    }

    private fun generateExceptionResponseDocs(
        operation: Operation,
        exceptions: Array<ApiExceptionExplanation>
    ) {
        val responses: ApiResponses = operation.responses

        val holders = exceptions
            .map { ExampleHolder.from(it) }
            .groupBy { it.httpStatus }

        addExamplesToResponses(responses, holders)
    }

    private fun addExamplesToResponses(
        responses: ApiResponses,
        holders: Map<Int, List<ExampleHolder>>
    ) {
        holders.forEach { (status, examples) ->
            val response = responses.computeIfAbsent(status.toString()) {
                ApiResponse()
                    .description("Error responses for status $status")
            }

            val content = response.content
                ?: Content().also { response.content = it }

            val mediaType = examples.first().mediaType
            val media = content.computeIfAbsent(mediaType) {
                MediaType()
            }

            examples.forEach { holder ->
                media.addExamples(holder.name, holder.holder)
            }
        }
    }

    private data class ExampleHolder(
        val httpStatus: Int,
        val name: String,
        val mediaType: String,
        val description: String,
        val holder: Example
    ) {
        companion object {
            fun from(annotation: ApiExceptionExplanation): ExampleHolder {
                val errorCode = getErrorCode(annotation)

                return ExampleHolder(
                    httpStatus = errorCode.httpStatus.value(),
                    name = if (annotation.name.isNotBlank()) annotation.name else errorCode.message,
                    mediaType = annotation.mediaType,
                    description = annotation.description,
                    holder = createExample(errorCode, annotation.summary, annotation.description)
                )
            }

            @Suppress("UNCHECKED_CAST")
            private fun getErrorCode(annotation: ApiExceptionExplanation): BaseErrorCode {
                val enumClass = annotation.value as KClass<out Enum<*>>
                val enumConstant = java.lang.Enum.valueOf(
                    enumClass.java,
                    annotation.constant
                )
                return enumConstant as BaseErrorCode
            }

            private fun createExample(
                errorCode: BaseErrorCode,
                summary: String,
                description: String
            ): Example {
                val response = Api(
                    success = false,
                    code = errorCode.httpStatus.value(),
                    message = errorCode.message,
                    data = null
                )

                return Example().apply {
                    value = response
                    this.summary = summary
                    this.description = description
                }
            }
        }
    }
}