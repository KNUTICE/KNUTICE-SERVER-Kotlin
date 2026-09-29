package com.fx.api.application.service

import com.fx.api.application.port.`in`.ImageCommandUseCase
import com.fx.api.application.port.out.ImagePersistencePort
import com.fx.api.application.port.out.ImageStoragePort
import com.fx.api.domain.Image
import com.fx.api.domain.ImageType
import com.fx.api.exception.ImageException
import com.fx.api.exception.errorcode.ImageErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
@Transactional(readOnly = true)
class ImageCommandService(
    private val imageStoragePort: ImageStoragePort,
    private val imagePersistencePort: ImagePersistencePort,
) : ImageCommandUseCase {

    /**
     * DEFAULT_IMAGE 는 하나만 둔다. 기존 이미지가 있으면 지우고 새로 저장하며, 서버 파일 이름은 `default.확장자` 다.
     * 그 밖의 유형은 UUID 파일 이름으로 저장만 한다.
     */
    @Transactional
    override fun uploadImage(imageFile: MultipartFile, type: ImageType): Image {
        val extension = imageFile.originalFilename?.substringAfterLast(".", "") ?: "png"
        val serverName = if (type == ImageType.DEFAULT_IMAGE) DEFAULT_IMAGE_SERVER_NAME else UUID.randomUUID().toString()

        if (type == ImageType.DEFAULT_IMAGE) {
            imagePersistencePort.findLatestByType(type)?.let {
                imagePersistencePort.delete(it)
                imageStoragePort.delete(it.serverName)
            }
        }

        val imageUrl = imageStoragePort.save(imageFile, serverName)
        return imagePersistencePort.save(
            Image(
                imageUrl = imageUrl,
                originalName = imageFile.originalFilename.orEmpty(),
                serverName = serverName,
                extension = extension,
                type = type,
            )
        )
    }

    @Transactional
    override fun deleteImage(imageId: String): Boolean {
        val image = imageId.toLongOrNull()?.let(imagePersistencePort::findById)
            ?: throw ImageException(ImageErrorCode.IMAGE_NOT_FOUND)

        imageStoragePort.delete(image.serverName)
        imagePersistencePort.delete(image)
        return true
    }

    companion object {
        private const val DEFAULT_IMAGE_SERVER_NAME = "default"
    }

}
