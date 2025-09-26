package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.fcm.FcmTokenSaveRequest
import com.fx.api.adapter.`in`.web.dto.fcm.FcmTokenUpdateRequest
import com.fx.api.adapter.`in`.web.swagger.FcmTokenOpenApiSwagger
import com.fx.api.application.port.`in`.FcmTokenCommandUseCase
import com.fx.global.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
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

    @PatchMapping // fcmToken 만 일부 변경하므로 Patch ...
    override fun updateFcmToken(
        @RequestHeader fcmToken: String, // 헤더 토큰이 새로운 값이며, Request body 는 oldFcmToken 이다.
        @RequestBody @Valid tokenUpdateRequest: FcmTokenUpdateRequest
    ): ResponseEntity<Api<Boolean>> {
        fcmTokenCommandUseCase.updateFcmToken(tokenUpdateRequest.toCommand(fcmToken))
        return Api.OK(true, "토큰이 업데이트되었습니다.")
    }


}