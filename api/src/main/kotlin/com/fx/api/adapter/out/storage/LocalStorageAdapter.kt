package com.fx.api.adapter.out.storage

import com.fx.api.application.port.out.ImageStoragePort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.util.UriComponentsBuilder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Component
class LocalStorageAdapter(
    @Value("\${url.scheme}") private val scheme: String,
    @Value("\${url.host}") private val host: String,
    @Value("\${server.port}") private val port: String,
    @Value("\${file.context-path}") private val contextPath: String,
    @Value("\${file.upload-dir}") private val uploadDir: Path,
) : ImageStoragePort {

    override fun save(imageFile: MultipartFile, serverName: String): String {
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir)
        }

        val extension = imageFile.originalFilename?.substringAfterLast(".", "") ?: "png"
        val fileName = "$serverName.$extension"
        val filePath = uploadDir.resolve(fileName)

        Files.copy(imageFile.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)

        return createImageUrl(fileName)
    }

    override fun delete(serverName: String) {
        val possibleExtensions = listOf("png", "jpg", "jpeg", "gif", "webp")
        for (ext in possibleExtensions) {
            val filePath = uploadDir.resolve("$serverName.$ext")
            if (Files.exists(filePath)) {
                Files.delete(filePath)
                break
            }
        }
    }

    private fun createImageUrl(fileName: String): String {
        val builder = UriComponentsBuilder.newInstance()
            .scheme(scheme)
            .host(host)
            .path("$contextPath$fileName")

        // localhost일 때만 포트 추가
        if (host.equals("localhost", ignoreCase = true)) {
            builder.port(port)
        }
        return builder.toUriString()
    }

}