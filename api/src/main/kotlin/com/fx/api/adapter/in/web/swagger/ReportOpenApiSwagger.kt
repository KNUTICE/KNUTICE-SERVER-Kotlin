package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.report.ReportSaveRequest
import com.fx.api.exception.errorcode.FcmTokenErrorCode
import com.fx.global.annotation.ApiExceptionExplanation
import com.fx.global.annotation.ApiResponseExplanations
import com.fx.global.api.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

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
    @Operation(summary = "문의사항 저장", description = "문의사항을 저장합니다. <br>" +
            "content : 5자에서 최대 500자까지 허용됩니다.<br>" +
            "deviceName : 최대 50자입니다. <br>version : 최대 50자입니다.")
    suspend fun saveReport(
        @RequestHeader fcmToken: String,
        @RequestBody @Valid reportSaveRequest: ReportSaveRequest
    ): ResponseEntity<Api<Boolean>>

}