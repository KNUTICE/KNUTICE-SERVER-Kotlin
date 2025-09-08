package com.fx.api.application.service

import com.fx.api.application.port.`in`.ImageQueryUseCase
import com.fx.api.application.port.out.ImagePersistencePort
import com.fx.api.domain.Image
import com.fx.api.domain.ImageType
import com.fx.api.exception.ImageException
import com.fx.api.exception.errorcode.ImageErrorCode
import org.springframework.stereotype.Service

@Service
class ImageQueryService(
    private val imagePersistencePort: ImagePersistencePort
) : ImageQueryUseCase {

    override fun getImages(type: ImageType): List<Image> {
        val images = imagePersistencePort.findAllByType(type)
        if (images.isEmpty()) {
            throw ImageException(ImageErrorCode.IMAGE_NOT_FOUND)
        }
        return images
    }

}