package com.fx.api.application.port.out.image

import org.springframework.web.multipart.MultipartFile

interface ImageStoragePort {

    fun save(imageFile: MultipartFile, serverName: String): String

    fun delete(serverName: String)

}