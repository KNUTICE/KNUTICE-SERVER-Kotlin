package com.fx.api.config.security.dto

import com.fx.api.domain.UserRole
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class AuthenticatedUser(
    val userId: String,
    val role: UserRole
) : UserDetails {
    override fun getAuthorities() = listOf(SimpleGrantedAuthority(role.description))
    override fun getPassword() = ""
    override fun getUsername() = userId
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true
}