package com.fx.api.application.port.`in`

import com.fx.api.domain.ImageType
import org.springframework.web.multipart.MultipartFile

interface ImageCommandUseCase {

    fun uploadImage(imageFile: MultipartFile, type: ImageType): String

    fun deleteImage(imageId: String): Boolean

}