package com.fx.api.adapter.out.persistence.repository;

import com.fx.api.adapter.out.persistence.document.ImageDocument
import com.fx.api.domain.ImageType;
import org.springframework.data.mongodb.repository.MongoRepository;

interface ImageMongoRepository : MongoRepository<ImageDocument, String> {

    fun findByType(type: ImageType): ImageDocument?

    fun findAllByType(type: ImageType): List<ImageDocument>

}
