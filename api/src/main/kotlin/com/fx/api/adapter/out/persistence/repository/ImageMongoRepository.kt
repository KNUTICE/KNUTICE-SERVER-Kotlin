package com.fx.api.adapter.out.persistence.repository;

import com.fx.api.adapter.out.persistence.document.ImageDocument
import com.fx.api.domain.ImageType;
import java.util.List;
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface ImageMongoRepository : CoroutineCrudRepository<ImageDocument, String> {

    suspend fun findByType(type: ImageType): ImageDocument?

    suspend fun findAllByType(type: ImageType): List<ImageDocument>

}
