package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.FcmTokenSaveRequest
import com.fx.api.adapter.`in`.web.swagger.FcmTokenOpenApiSwagger
import com.fx.api.application.port.`in`.FcmTokenCommandUseCase
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.api.Api
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/open-api/v1/fcm-tokens")
class FcmTokenOpenApiAdapter(
    private val fcmTokenCommandUseCase: FcmTokenCommandUseCase
) : FcmTokenOpenApiSwagger {

    @PostMapping
    override fun saveFcmToken(
        @RequestHeader fcmToken: String,
        @RequestBody @Valid tokenSaveRequest: FcmTokenSaveRequest
    ): ResponseEntity<Api<Boolean>> {
        fcmTokenCommandUseCase.saveFcmToken(tokenSaveRequest.toCommand(fcmToken))
        return Api.OK(true, "토큰이 저장되었습니다.")
    }


}