package com.fx.api.adapter.out.storage

import com.fx.api.application.port.out.ImageStoragePort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.nio.file.Files
import java.nio.file.Path

/**
 * java.nio.file 는 Blocking 동작이므로 별도 스레드 풀로 처리
 *
 * @author 이동섭
 * @since 2025-10-21
 */
@Component
class LocalStorageAdapter(
    @Value("\${url.scheme}") private val scheme: String,
    @Value("\${url.host}") private val host: String,
    @Value("\${server.port}") private val port: String,
    @Value("\${file.context-path}") private val contextPath: String,
    @Value("\${file.upload-dir}") private val uploadDir: Path,
) : ImageStoragePort {

    override suspend fun save(imageFile: FilePart, serverName: String): String = withContext(Dispatchers.IO) {
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir)
        }

        val extension = imageFile.filename().substringAfterLast(".", "")
        val fileName = "$serverName.$extension"
        val filePath = uploadDir.resolve(fileName)

        imageFile.transferTo(filePath).awaitSingleOrNull()

        createImageUrl(fileName)
    }

    override suspend fun delete(serverName: String) = withContext(Dispatchers.IO) {
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