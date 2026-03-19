package com.fx.api.adapter.out.persistence.document

import com.fx.api.domain.User
import com.fx.api.domain.UserRole
import com.fx.global.adapter.out.persistence.document.base.MongoBaseDocument
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "user")
class UserDocument(

    @Id
    var id: String? = null,

    var email: String,

    var password: String,

    var nickname: String,

    var role: UserRole

) : MongoBaseDocument() {

    fun toDomain(): User =
        User(
            id = id,
            email = email,
            password = password,
            nickname = nickname,
            role = role,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    companion object {
        fun from(user: User): UserDocument =
            UserDocument(
                id = user.id,
                email = user.email,
                password = user.password,
                nickname = user.nickname,
                role = user.role
            ).apply {
                this.createdAt = user.createdAt
                this.updatedAt = user.updatedAt
            }
    }
}