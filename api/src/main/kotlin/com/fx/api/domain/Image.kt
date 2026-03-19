package com.fx.api.domain

import org.springframework.http.codec.multipart.FilePart
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
            imageFile: FilePart,
            imageUrl: String,
            serverName: String,
            extension: String,
            type: ImageType
        ): Image =
            Image(
                imageUrl = imageUrl,
                originalName = imageFile.filename(),
                serverName = serverName,
                extension = extension,
                type = type
            )
    }
}