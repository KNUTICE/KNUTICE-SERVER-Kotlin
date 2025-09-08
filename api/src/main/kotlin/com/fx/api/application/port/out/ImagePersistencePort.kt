package com.fx.api.application.port.out

import com.fx.api.domain.Image
import com.fx.api.domain.ImageType

interface ImagePersistencePort {

    fun save(image: Image): Image

    fun findByType(type: ImageType): Image?

    fun findAllByType(type: ImageType): List<Image>

    fun findById(imageId: String): Image?

    fun delete(imageId: String)

}