package com.fx.api.application.port.`in`

import com.fx.api.domain.Image
import com.fx.api.domain.ImageType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.multipart.MultipartFile

interface ImageCommandUseCase {

    suspend fun uploadImage(imageFile: FilePart, type: ImageType): Image

    suspend fun deleteImage(imageId: String): Boolean

}