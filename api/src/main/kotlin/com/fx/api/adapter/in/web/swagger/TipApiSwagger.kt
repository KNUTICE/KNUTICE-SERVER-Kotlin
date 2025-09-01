package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.TipSaveRequest
import com.fx.global.api.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "TIP 관리 API - ADMIN")
interface TipApiSwagger {

    @Operation(summary = "팁 저장", description = "새로운 팁을 저장합니다.")
    fun saveTip(@RequestBody tipSaveRequest: TipSaveRequest): ResponseEntity<Api<Boolean>>

    @Operation(summary = "팁 삭제", description = "tipId 로 팁을 삭제합니다.")
    fun deleteTip(@PathVariable tipId: String): ResponseEntity<Api<Boolean>>

}