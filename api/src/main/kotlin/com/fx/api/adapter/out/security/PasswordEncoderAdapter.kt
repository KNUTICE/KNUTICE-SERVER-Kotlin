package com.fx.api.adapter.out.security

import com.fx.api.application.port.out.PasswordEncoderPort
import com.fx.global.annotation.SecurityAdapter
import org.springframework.security.crypto.password.PasswordEncoder

@SecurityAdapter
class PasswordEncoderAdapter(
    private val passwordEncoder: PasswordEncoder
) : PasswordEncoderPort {

    override fun encode(rawPassword: String): String {
        return passwordEncoder.encode(rawPassword)
    }

    override fun matches(rawPassword: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, encodedPassword)
    }

}