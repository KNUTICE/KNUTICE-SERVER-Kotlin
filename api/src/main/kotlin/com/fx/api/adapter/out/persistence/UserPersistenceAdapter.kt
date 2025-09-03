package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.document.UserDocument
import com.fx.api.adapter.out.persistence.repository.UserMongoRepository
import com.fx.api.application.port.out.UserPersistencePort
import com.fx.api.domain.User
import com.fx.global.annotation.PersistenceAdapter
import org.springframework.security.web.webauthn.management.UserCredentialRepository

@PersistenceAdapter
class UserPersistenceAdapter(
    private val userMongoRepository: UserMongoRepository
) : UserPersistencePort {

    override fun save(user: User): User =
        userMongoRepository.save(UserDocument.from(user)).toDomain()

    override fun existsByEmail(email: String): Boolean =
        userMongoRepository.existsByEmail(email)

    override fun existsByNickname(nickname: String): Boolean =
        userMongoRepository.existsByNickname(nickname)

    override fun findByEmail(email: String): User? =
        userMongoRepository.findByEmail(email)?.orElse(null)?.toDomain()

}