package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.UserRepository
import com.fx.api.application.port.out.user.UserPersistencePort
import com.fx.api.domain.User
import com.fx.common.annotation.PersistenceAdapter

@PersistenceAdapter
class UserPersistenceAdapter(
    private val userRepository: UserRepository,
) : UserPersistencePort {

    override fun save(user: User): User =
        userRepository.save(user)

    override fun existsByEmail(email: String): Boolean =
        userRepository.existsByEmail(email)

    override fun existsByNickname(nickname: String): Boolean =
        userRepository.existsByNickname(nickname)

    override fun findByEmail(email: String): User? =
        userRepository.findByEmail(email)

}
