package com.fx.api.application.port.out

import org.springframework.http.codec.multipart.FilePart

interface ImageStoragePort {

    suspend fun save(imageFile: FilePart, serverName: String): String

    suspend fun delete(serverName: String)

}