package com.fx.api.application.port.out

import com.fx.api.domain.Image
import com.fx.api.domain.ImageType

interface ImagePersistencePort {

    suspend fun save(image: Image): Image

    suspend fun findByType(type: ImageType): Image?

    suspend fun findAllByType(type: ImageType): List<Image>

    suspend fun findById(imageId: String): Image?

    suspend fun delete(imageId: String)

}