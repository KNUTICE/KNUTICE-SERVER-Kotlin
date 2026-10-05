package com.fx.api.application.port.out.image

import com.fx.api.domain.Image
import com.fx.api.domain.ImageType

interface ImagePersistencePort {

    fun save(image: Image): Image

    /** [type] 이미지 중 가장 최근 것. */
    fun findLatestByType(type: ImageType): Image?

    /** [type] 이미지 전체. 등록 순. */
    fun findAllByType(type: ImageType): List<Image>

    fun findById(imageId: Long): Image?

    fun delete(image: Image)

}
