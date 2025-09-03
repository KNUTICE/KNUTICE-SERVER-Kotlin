package com.fx.api.domain

enum class UserRole(
    private val description: String,
) {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER")
}