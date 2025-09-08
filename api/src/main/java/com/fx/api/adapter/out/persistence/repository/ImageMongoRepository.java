package com.fx.api.adapter.out.persistence.repository;

import com.fx.api.adapter.out.persistence.document.ImageDocument;
import com.fx.api.domain.ImageType;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ImageMongoRepository extends MongoRepository<ImageDocument, String> {

    Optional<ImageDocument> findByType(ImageType type);

}
