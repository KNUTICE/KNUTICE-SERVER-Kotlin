package com.fx.api.application.service

import com.fx.api.adapter.out.storage.LocalStorageAdapter
import com.fx.api.application.port.`in`.ImageCommandUseCase
import com.fx.api.application.port.out.ImagePersistencePort
import com.fx.api.application.port.out.ImageStoragePort
import com.fx.api.domain.Image
import com.fx.api.domain.ImageType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class ImageCommandService(
    private val imageStoragePort: ImageStoragePort,
    private val imagePersistencePort: ImagePersistencePort,
) : ImageCommandUseCase {

    private val log = LoggerFactory.getLogger(ImageCommandService::class.java)

    /**
     * DEFAULT_IMAGE 는 하나만 존재 → 있으면 교체, 없으면 새로 저장 <br>
     * DEFAULT_IMAGE 의 서버 파일 이름은 **default.확장자** 로 제한한다. <br>
     * 확장자가 다른 경우 이미지를 제거하고 새로 저장 <br>
     * TIP_IMAGE 등은 단순 저장
     */
    override fun uploadImage(imageFile: MultipartFile, type: ImageType): String {

        val extension = imageFile.originalFilename?.substringAfterLast(".", "") ?: "png"
        val serverName = if (type == ImageType.DEFAULT_IMAGE) "default" else UUID.randomUUID().toString()

        if (type == ImageType.DEFAULT_IMAGE) {
            // 기존 이미지 있으면 삭제
            val existingImage = imagePersistencePort.findByType(type)
            existingImage?.let {
                imagePersistencePort.delete(it.id!!)
                imageStoragePort.delete(it.serverName)
            }
        }

        val imageUrl = imageStoragePort.save(imageFile, serverName)

        val newImage = Image.createImage(imageFile, imageUrl, serverName, extension, type)
        imagePersistencePort.save(newImage)

        return imageUrl
    }

    override fun deleteImage(imageId: String): Boolean {
        val existingImage = imagePersistencePort.findById(imageId) ?: return false
        imageStoragePort.delete(existingImage.serverName)
        imagePersistencePort.delete(imageId)
        return true
    }

}