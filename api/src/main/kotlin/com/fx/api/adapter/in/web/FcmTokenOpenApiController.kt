package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.FcmTokenRequest
import com.fx.api.application.port.`in`.FcmTokenCommandUseCase
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.api.Api
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/open-api/v1/fcm-tokens")
class FcmTokenOpenApiController(
    private val fcmTokenCommandUseCase: FcmTokenCommandUseCase
) {

    @Operation(summary = "토큰 저장/업데이트", description = "토큰이 없는 경우 새로 저장하며 있는 경우 isActive 값을 true 로 활성화하여 저장합니다.")
    @PostMapping
    fun saveFcmToken(@RequestBody tokenRequest: FcmTokenRequest): ResponseEntity<Api<Boolean>> {
        fcmTokenCommandUseCase.saveFcmToken(tokenRequest.toCommand())
        return Api.OK(true, "토큰이 저장되었습니다.")
    }


}