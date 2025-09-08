package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.document.ImageDocument
import com.fx.api.adapter.out.persistence.repository.ImageMongoRepository
import com.fx.api.application.port.out.ImagePersistencePort
import com.fx.api.domain.Image
import com.fx.api.domain.ImageType
import com.fx.global.annotation.PersistenceAdapter

@PersistenceAdapter
class ImagePersistenceAdapter(
    private val imageMongoRepository: ImageMongoRepository,
) : ImagePersistencePort {

    override fun save(image: Image): Image =
        imageMongoRepository.save(ImageDocument.from(image)).toDomain()

    override fun findByType(type: ImageType): Image? =
        imageMongoRepository.findByType(type)?.orElse(null)?.toDomain()

    override fun findAllByType(type: ImageType): List<Image> =
        imageMongoRepository.findAllByType(type).map { it.toDomain() }

    override fun findById(imageId: String): Image? =
        imageMongoRepository.findById(imageId).orElse(null)?.toDomain()

    override fun delete(imageId: String) =
        imageMongoRepository.deleteById(imageId)
}