package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.tip.TipSaveRequest
import com.fx.api.adapter.`in`.web.swagger.TipApiSwagger
import com.fx.api.application.port.`in`.TipCommandUseCase
import com.fx.global.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@WebInputAdapter
@RequestMapping("/api/v1/tips")
class TipApiAdapter(
    private val tipCommandUseCase: TipCommandUseCase
) : TipApiSwagger {

    @PostMapping
    override fun saveTip(@RequestBody @Valid tipSaveRequest: TipSaveRequest): ResponseEntity<Api<Boolean>> =
        Api.OK(tipCommandUseCase.saveTip(tipSaveRequest.toCommand()), "TIP 이 저장되었습니다.")

    @DeleteMapping("/{tipId}")
    override fun deleteTip(@PathVariable tipId: String): ResponseEntity<Api<Boolean>> =
        Api.OK(tipCommandUseCase.deleteTip(tipId), "TIP 이 삭제되었습니다.")

}