package com.fx.api.adapter.out.persistence.repository;

import com.fx.api.adapter.out.persistence.document.UserDocument;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserMongoRepository extends MongoRepository<UserDocument, String> {

    Boolean existsByEmail(String email);

    Boolean existsByNickname(String nickname);

    Optional<UserDocument> findByEmail(String email);

}
