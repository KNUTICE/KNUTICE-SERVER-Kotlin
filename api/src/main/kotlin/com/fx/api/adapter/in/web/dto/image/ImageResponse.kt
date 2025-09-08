package com.fx.api.adapter.`in`.web.dto.image

import com.fx.api.domain.Image
import com.fx.api.domain.ImageType

data class ImageResponse(

    val imageId: String,
    val imageUrl: String,
    val type: ImageType

) {

    companion object {

        fun from(image: Image) =
            ImageResponse(
                image.id!!,
                image.imageUrl,
                image.type
            )

    }

}
