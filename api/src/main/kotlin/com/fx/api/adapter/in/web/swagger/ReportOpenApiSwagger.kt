package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.ReportRequest
import com.fx.api.exception.errorcode.FcmTokenErrorCode
import com.fx.global.annotation.ApiExceptionExplanation
import com.fx.global.annotation.ApiResponseExplanations
import com.fx.global.api.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "문의사항 API")
interface ReportOpenApiSwagger {

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "문의사항 저장 실패",
                description = "Fcm token 이 존재하지 않는 경우",
                value = FcmTokenErrorCode::class,
                constant = "TOKEN_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "문의사항 저장", description = "문의사항을 저장합니다.")
    fun saveReport(
        @RequestBody reportRequest: ReportRequest
    ): ResponseEntity<Api<Boolean>>

}