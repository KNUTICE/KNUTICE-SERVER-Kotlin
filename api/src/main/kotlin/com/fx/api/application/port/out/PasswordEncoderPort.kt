package com.fx.api.application.port.out

interface PasswordEncoderPort {

    suspend fun encode(rawPassword: String): String

    fun matches(rawPassword: String, encodedPassword: String): Boolean

}