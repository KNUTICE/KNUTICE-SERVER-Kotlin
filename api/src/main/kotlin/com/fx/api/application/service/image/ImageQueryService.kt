package com.fx.api.application.service.image

import com.fx.api.application.port.`in`.image.ImageQueryUseCase
import com.fx.api.application.port.out.image.ImagePersistencePort
import com.fx.api.domain.Image
import com.fx.api.domain.ImageType
import com.fx.api.exception.ImageException
import com.fx.api.exception.errorcode.ImageErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ImageQueryService(
    private val imagePersistencePort: ImagePersistencePort,
) : ImageQueryUseCase {

    override fun getImages(type: ImageType): List<Image> {
        val images = imagePersistencePort.findAllByType(type)
        if (images.isEmpty()) {
            throw ImageException(ImageErrorCode.IMAGE_NOT_FOUND)
        }
        return images
    }

}
