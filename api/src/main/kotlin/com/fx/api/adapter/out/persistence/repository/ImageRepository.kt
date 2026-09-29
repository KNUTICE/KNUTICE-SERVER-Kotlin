package com.fx.api.adapter.out.persistence.repository

import com.fx.api.domain.Image
import com.fx.api.domain.ImageType
import org.springframework.data.jpa.repository.JpaRepository

interface ImageRepository : JpaRepository<Image, Long> {

    fun findFirstByTypeOrderByIdDesc(type: ImageType): Image?

    fun findAllByTypeOrderByIdAsc(type: ImageType): List<Image>

}
