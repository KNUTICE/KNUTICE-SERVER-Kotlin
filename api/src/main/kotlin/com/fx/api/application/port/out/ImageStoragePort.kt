package com.fx.api.application.port.out

import org.springframework.web.multipart.MultipartFile

interface ImageStoragePort {

    suspend fun save(file: MultipartFile, serverName: String): String

    fun delete(serverName: String)

}