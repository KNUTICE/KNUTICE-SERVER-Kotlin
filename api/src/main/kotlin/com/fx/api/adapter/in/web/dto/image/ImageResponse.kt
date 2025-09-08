package com.fx.api.adapter.`in`.web.dto.image

import com.fx.api.adapter.`in`.web.dto.notice.NoticeResponse
import com.fx.api.domain.Image
import com.fx.api.domain.ImageType
import com.fx.global.domain.Notice

data class ImageResponse(

    val imageId: String,
    val imageUrl: String,
    val type: ImageType

) {

    companion object {

        fun from(image: Image): ImageResponse =
            ImageResponse(
                image.id!!,
                image.imageUrl,
                image.type
            )

        fun from(images: List<Image>): List<ImageResponse> =
            images.map { this.from(it) }

    }

}
