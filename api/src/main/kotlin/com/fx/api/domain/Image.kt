package com.fx.api.domain

import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

data class Image(
    val id: String? = null,
    val imageUrl: String,
    val originalName: String,
    val serverName: String,
    val extension: String,
    val type: ImageType,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
) {

    companion object {
        fun createImage(
            imageFile: MultipartFile,
            imageUrl: String,
            serverName: String,
            extension: String,
            type: ImageType
        ): Image =
            Image(
                imageUrl = imageUrl,
                originalName = imageFile.originalFilename!!,
                serverName = serverName,
                extension = extension,
                type = type
            )
    }
}