package com.fx.api.adapter.`in`.web

import com.fx.api.adapter.`in`.web.dto.image.ImageResponse
import com.fx.api.application.port.`in`.ImageQueryUseCase
import com.fx.api.domain.ImageType
import com.fx.global.annotation.hexagonal.WebInputAdapter
import com.fx.global.api.Api
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@WebInputAdapter
@RequestMapping("/open-api/v1/images")
class ImageOpenApiAdapter(
    private val imageQueryUseCase: ImageQueryUseCase
) {

    @GetMapping
    fun getImages(@RequestParam type: ImageType): ResponseEntity<Api<List<ImageResponse>>> =
        Api.OK(ImageResponse.from(imageQueryUseCase.getImages(type)))

}