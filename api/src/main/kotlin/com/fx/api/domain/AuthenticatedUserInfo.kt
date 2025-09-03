package com.fx.api.domain

data class AuthenticatedUserInfo(
    val userId: String,
    val role: UserRole
)