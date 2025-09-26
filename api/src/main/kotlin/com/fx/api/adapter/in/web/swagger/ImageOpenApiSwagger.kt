package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.image.ImageResponse
import com.fx.api.domain.ImageType
import com.fx.api.exception.errorcode.ImageErrorCode
import com.fx.global.annotation.ApiExceptionExplanation
import com.fx.global.annotation.ApiResponseExplanations
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "IMAGE 관리 API")
interface ImageOpenApiSwagger {

    @ApiResponseExplanations(
        errors = [
            ApiExceptionExplanation(
                name = "이미지 조회 실패",
                description = "이미지가 존재하지 않는 경우",
                value = ImageErrorCode::class,
                constant = "IMAGE_NOT_FOUND"
            ),
        ]
    )
    @Operation(summary = "이미지 조회", description = "Type 으로 이미지 목록을 조회합니다.")
    fun getImages(@RequestParam type: ImageType): ResponseEntity<Api<List<ImageResponse>>>

}