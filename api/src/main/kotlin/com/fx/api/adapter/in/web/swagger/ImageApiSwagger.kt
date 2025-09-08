package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.image.ImageResponse
import com.fx.api.adapter.`in`.web.dto.tip.TipSaveRequest
import com.fx.api.domain.ImageType
import com.fx.api.exception.errorcode.ImageErrorCode
import com.fx.global.annotation.ApiExceptionExplanation
import com.fx.global.annotation.ApiResponseExplanations
import com.fx.global.api.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@Tag(name = "IMAGE 관리 API - ADMIN")
interface ImageApiSwagger {


    @Operation(summary = "이미지 업로드", description = "Type 으로 이미지를 업로드합니다. <br>" +
            "DEFAULT_IMAGE 는 단 하나의 이미지만 저장됩니다.")
    fun uploadImage(
        @RequestParam("image") image: MultipartFile,
        @RequestParam type: ImageType
    ): ResponseEntity<Api<ImageResponse>>


    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "이미지 삭제 실패",
                description = "이미지가 존재하지 않는 경우",
                value = ImageErrorCode::class,
                constant = "IMAGE_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "이미지 삭제", description = "삭제할 이미지가 없는 경우 예외가 발생합니다.")
    fun deleteImage(@RequestParam imageId: String): ResponseEntity<Api<Boolean>>

}