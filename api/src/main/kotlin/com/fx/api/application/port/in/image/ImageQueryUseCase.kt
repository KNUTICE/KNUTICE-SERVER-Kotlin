package com.fx.api.application.port.`in`.image

import com.fx.api.domain.Image
import com.fx.api.domain.ImageType

interface ImageQueryUseCase {

    fun getImages(type: ImageType): List<Image>

}