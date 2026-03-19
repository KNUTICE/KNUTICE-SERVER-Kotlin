package com.fx.api.adapter.out.security

import com.fx.api.application.port.out.PasswordEncoderPort
import com.fx.global.annotation.SecurityAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.security.crypto.password.PasswordEncoder

@SecurityAdapter
class PasswordEncoderAdapter(
    private val passwordEncoder: PasswordEncoder
) : PasswordEncoderPort {

    override suspend fun encode(rawPassword: String): String = withContext(Dispatchers.Default) {
        passwordEncoder.encode(rawPassword)
    }

    override fun matches(rawPassword: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, encodedPassword)
    }

}