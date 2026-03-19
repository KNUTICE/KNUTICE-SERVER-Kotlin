package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.document.ImageDocument
import com.fx.api.adapter.out.persistence.repository.ImageMongoRepository
import com.fx.api.application.port.out.ImagePersistencePort
import com.fx.api.domain.Image
import com.fx.api.domain.ImageType
import com.fx.global.annotation.PersistenceAdapter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList

@PersistenceAdapter
class ImagePersistenceAdapter(
    private val imageMongoRepository: ImageMongoRepository,
) : ImagePersistencePort {

    override suspend fun save(image: Image): Image =
        imageMongoRepository.save(ImageDocument.from(image)).toDomain()

    override suspend fun findByType(type: ImageType): Image? =
        imageMongoRepository.findByType(type)?.toDomain()

    override suspend fun findAllByType(type: ImageType): List<Image> =
        imageMongoRepository.findAllByType(type)
            .map { it.toDomain() }
            .toList()

    override suspend fun findById(imageId: String): Image? =
        imageMongoRepository.findById(imageId)?.toDomain()

    override suspend fun delete(imageId: String) =
        imageMongoRepository.deleteById(imageId)

}