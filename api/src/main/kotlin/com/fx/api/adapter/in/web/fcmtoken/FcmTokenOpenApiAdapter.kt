package com.fx.api.adapter.`in`.web.fcmtoken

import com.fx.api.adapter.`in`.web.fcmtoken.dto.FcmTokenLanguageResponse
import com.fx.api.adapter.`in`.web.fcmtoken.dto.FcmTokenLanguageUpdateRequest
import com.fx.api.adapter.`in`.web.fcmtoken.dto.FcmTokenSaveRequest
import com.fx.api.adapter.`in`.web.fcmtoken.dto.FcmTokenUpdateRequest
import com.fx.api.application.port.`in`.fcmtoken.FcmTokenCommandUseCase
import com.fx.api.application.port.`in`.fcmtoken.FcmTokenQueryUseCase
import com.fx.common.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/open-api/v1/fcm-tokens")
class FcmTokenOpenApiAdapter(
    private val fcmTokenCommandUseCase: FcmTokenCommandUseCase,
    private val fcmTokenQueryUseCase: FcmTokenQueryUseCase,
) : FcmTokenOpenApiSwagger {

    private val log = LoggerFactory.getLogger(FcmTokenOpenApiAdapter::class.java)

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
        log.info("FcmToken updated: old=${tokenUpdateRequest.oldFcmToken} -> new=$fcmToken")
        return Api.OK(true, "토큰이 업데이트되었습니다.")
    }

    @GetMapping("/language")
    override fun getLanguage(
        @RequestHeader fcmToken: String
    ): ResponseEntity<Api<FcmTokenLanguageResponse>> =
        Api.OK(FcmTokenLanguageResponse.from(fcmTokenQueryUseCase.getLanguage(fcmToken)), "알림 언어 조회 성공")

    @PatchMapping("/language")
    override fun updateLanguage(
        @RequestHeader fcmToken: String,
        @RequestBody @Valid languageUpdateRequest: FcmTokenLanguageUpdateRequest
    ): ResponseEntity<Api<Boolean>> {
        fcmTokenCommandUseCase.updateLanguage(languageUpdateRequest.toCommand(fcmToken))
        return Api.OK(true, "알림 언어가 변경되었습니다.")
    }


}