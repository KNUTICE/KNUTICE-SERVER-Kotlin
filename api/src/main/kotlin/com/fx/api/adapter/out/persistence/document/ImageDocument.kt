package com.fx.api.adapter.out.persistence.document

import com.fx.api.domain.Image
import com.fx.api.domain.ImageType
import com.fx.global.adapter.out.persistence.base.MongoBaseDocument
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "image")
class ImageDocument(

    @Id
    var id: String? = null,

    var imageUrl: String,

    var originalName: String,

    var serverName: String,

    var extension: String,

    var type: ImageType

) : MongoBaseDocument() {

    fun toDomain(): Image =
        Image(
            id = id,
            imageUrl = imageUrl,
            originalName = originalName,
            serverName = serverName,
            extension = extension,
            type = type,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    companion object {
        fun from(image: Image): ImageDocument =
            ImageDocument(
                id = image.id,
                imageUrl = image.imageUrl,
                originalName = image.originalName,
                serverName = image.serverName,
                extension = image.extension,
                type = image.type
            ).apply {
                this.createdAt = image.createdAt
                this.updatedAt = image.updatedAt
            }
    }
}