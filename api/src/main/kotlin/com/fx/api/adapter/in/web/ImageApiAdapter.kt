package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.image.ImageResponse
import com.fx.api.adapter.`in`.web.dto.image.ImageUploadRequest
import com.fx.api.adapter.`in`.web.swagger.ImageApiSwagger
import com.fx.api.application.port.`in`.ImageCommandUseCase
import com.fx.global.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@WebInputAdapter
@RequestMapping("/api/v1/images")
class ImageApiAdapter(
    private val imageCommandUseCase: ImageCommandUseCase
) : ImageApiSwagger {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    override suspend fun uploadImage(
        @ModelAttribute uploadRequest: ImageUploadRequest
    ): ResponseEntity<Api<ImageResponse>> =
        Api.OK(
            ImageResponse.from(imageCommandUseCase.uploadImage(uploadRequest.image, uploadRequest.type)),
            "${uploadRequest.type} 가 저장되었습니다."
        )

    @DeleteMapping
    override suspend fun deleteImage(@RequestParam imageId: String): ResponseEntity<Api<Boolean>> =
        Api.OK(imageCommandUseCase.deleteImage(imageId), "이미지가 제거되었습니다.")

}