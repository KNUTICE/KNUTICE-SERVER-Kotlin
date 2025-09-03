package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.FcmTokenSaveRequest
import com.fx.global.api.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@Tag(name = "[토큰 관리 API]")
interface FcmTokenOpenApiSwagger {

    @Operation(summary = "토큰 저장/업데이트", description = "토큰이 없는 경우 새로 저장하며 있는 경우 isActive 값을 true 로 활성화하여 저장합니다.")
    fun saveFcmToken(
        @RequestHeader fcmToken: String,
        @RequestBody @Valid tokenSaveRequest: FcmTokenSaveRequest
    ): ResponseEntity<Api<Boolean>>
}