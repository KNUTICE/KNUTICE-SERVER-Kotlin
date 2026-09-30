package com.fx.api.adapter.`in`.web.image.dto

import com.fx.api.domain.Image
import com.fx.api.domain.ImageType

data class ImageResponse(
    /** TSID 를 문자열로 내보낸다 (JS 숫자 정밀도 한계). */
    val imageId: String,
    val imageUrl: String,
    val type: ImageType
) {

    companion object {

        fun from(image: Image): ImageResponse =
            ImageResponse(
                imageId = requireNotNull(image.id).toString(),
                imageUrl = image.imageUrl,
                type = image.type
            )

        fun from(images: List<Image>): List<ImageResponse> =
            images.map {
                this.from(it)
            }
    }

}
