package com.fx.api.domain

data class AuthenticatedUserInfo(
    val userId: Long,
    val role: UserRole
)