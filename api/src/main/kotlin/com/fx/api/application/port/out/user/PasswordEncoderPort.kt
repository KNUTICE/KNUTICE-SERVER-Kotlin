package com.fx.api.application.port.out.user

interface PasswordEncoderPort {

    fun encode(rawPassword: String): String

    fun matches(rawPassword: String, encodedPassword: String): Boolean

}