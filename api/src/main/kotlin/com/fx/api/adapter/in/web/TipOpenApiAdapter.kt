package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.TipResponse
import com.fx.api.adapter.`in`.web.swagger.TipOpenApiSwagger
import com.fx.api.application.port.`in`.TipQueryUseCase
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.api.Api
import com.fx.global.domain.DeviceType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@WebInputAdapter
@RequestMapping("/open-api/v1/tips")
class TipOpenApiAdapter(
    private val tipQueryUseCase: TipQueryUseCase
) : TipOpenApiSwagger {
    // TODO API 요청 수

    @GetMapping
    override fun getTips(
        @RequestParam(defaultValue = "iOS") deviceType: DeviceType
    ): ResponseEntity<Api<List<TipResponse>>> =
        Api.OK(TipResponse.from(tipQueryUseCase.getTips(deviceType)))

}