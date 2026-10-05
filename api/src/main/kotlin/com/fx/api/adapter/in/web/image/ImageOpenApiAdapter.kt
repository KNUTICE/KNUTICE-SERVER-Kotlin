package com.fx.api.adapter.`in`.web.image

import com.fx.api.adapter.`in`.web.image.dto.ImageResponse
import com.fx.api.application.port.`in`.image.ImageQueryUseCase
import com.fx.api.domain.ImageType
import com.fx.common.annotation.hexagonal.WebInputAdapter
import io.github.seob7.Api
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@WebInputAdapter
@RequestMapping("/open-api/v1/images")
class ImageOpenApiAdapter(
    private val imageQueryUseCase: ImageQueryUseCase
) : ImageOpenApiSwagger {

    @GetMapping
    override fun getImages(@RequestParam type: ImageType): ResponseEntity<Api<List<ImageResponse>>> =
        Api.OK(ImageResponse.from(imageQueryUseCase.getImages(type)))

}