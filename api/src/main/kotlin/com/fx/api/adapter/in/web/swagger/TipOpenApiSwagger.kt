package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.TipResponse
import com.fx.api.exception.errorcode.FcmTokenErrorCode
import com.fx.api.exception.errorcode.TipErrorCode
import com.fx.global.annotation.ApiExceptionExplanation
import com.fx.global.annotation.ApiResponseExplanations
import com.fx.global.api.Api
import com.fx.global.domain.DeviceType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "TIP API")
interface TipOpenApiSwagger {

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "팁 조회 실패",
                description = "팁 목록이 존재하지 않는 경우",
                value = TipErrorCode::class,
                constant = "TIP_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "팁 조회", description = "등록된 팁 목록을 최신순으로 조회합니다. deviceType 이 생략된 경우 iOS 기준으로 조회됩니다.")
    fun getTips(
        @RequestParam(defaultValue = "iOS") deviceType: DeviceType
    ): ResponseEntity<Api<List<TipResponse>>>

}