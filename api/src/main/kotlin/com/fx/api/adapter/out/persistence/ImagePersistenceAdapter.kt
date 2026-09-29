package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.ImageRepository
import com.fx.api.application.port.out.ImagePersistencePort
import com.fx.api.domain.Image
import com.fx.api.domain.ImageType
import com.fx.common.annotation.PersistenceAdapter

@PersistenceAdapter
class ImagePersistenceAdapter(
    private val imageRepository: ImageRepository,
) : ImagePersistencePort {

    override fun save(image: Image): Image = imageRepository.save(image)

    override fun findLatestByType(type: ImageType): Image? = imageRepository.findFirstByTypeOrderByIdDesc(type)

    override fun findAllByType(type: ImageType): List<Image> = imageRepository.findAllByTypeOrderByIdAsc(type)

    override fun findById(imageId: Long): Image? = imageRepository.findById(imageId).orElse(null)

    override fun delete(image: Image) = imageRepository.delete(image)

}
