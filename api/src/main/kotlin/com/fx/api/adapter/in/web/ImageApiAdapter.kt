package com.fx.api.adapter.`in`.web

import com.fx.api.application.port.`in`.ImageCommandUseCase
import com.fx.api.domain.ImageType
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.api.Api
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@WebInputAdapter
@RequestMapping("/open-api/v1/images")
class ImageApiAdapter(
    private val imageCommandUseCase: ImageCommandUseCase
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadImage(
        @RequestParam("image") image: MultipartFile,
        @RequestParam type: ImageType
    ): ResponseEntity<Api<String>> =
        Api.OK(imageCommandUseCase.uploadImage(image, type), "$type 가 저장되었습니다.")

    @DeleteMapping
    fun deleteImage(@RequestParam imageId: String): ResponseEntity<Api<Boolean>> =
        Api.OK(imageCommandUseCase.deleteImage(imageId), "이미지가 제거되었습니다.")

}