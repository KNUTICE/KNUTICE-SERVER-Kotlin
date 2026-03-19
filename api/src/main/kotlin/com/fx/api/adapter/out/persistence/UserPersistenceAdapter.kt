package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.document.UserDocument
import com.fx.api.adapter.out.persistence.repository.UserMongoRepository
import com.fx.api.application.port.out.UserPersistencePort
import com.fx.api.domain.User
import com.fx.global.annotation.PersistenceAdapter

@PersistenceAdapter
class UserPersistenceAdapter(
    private val userMongoRepository: UserMongoRepository
) : UserPersistencePort {

    override suspend fun save(user: User): User =
        userMongoRepository.save(UserDocument.from(user)).toDomain()

    override suspend fun existsByEmail(email: String): Boolean =
        userMongoRepository.existsByEmail(email)

    override suspend fun existsByNickname(nickname: String): Boolean =
        userMongoRepository.existsByNickname(nickname)

    override suspend fun findByEmail(email: String): User? =
        userMongoRepository.findByEmail(email)?.toDomain()

}