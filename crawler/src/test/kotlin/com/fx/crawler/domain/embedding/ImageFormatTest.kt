package com.fx.crawler.domain.embedding

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ImageFormatTest {

    @Test
    fun `파일 앞부분으로 PNG · JPEG · WEBP 를 알아보고 나머지는 받지 않는다`() {
        val png = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0)
        val jpeg = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte())
        val webp = "RIFF".toByteArray() + byteArrayOf(0, 0, 0, 0) + "WEBPVP8 ".toByteArray()

        assertThat(ImageFormat.detect(png)).isEqualTo(ImageFormat.PNG)
        assertThat(ImageFormat.detect(jpeg)).isEqualTo(ImageFormat.JPEG)
        assertThat(ImageFormat.detect(webp)).isEqualTo(ImageFormat.WEBP)
        assertThat(ImageFormat.detect("GIF89a".toByteArray())).isNull()
        assertThat(ImageFormat.detect("RIFF".toByteArray() + byteArrayOf(0, 0, 0, 0) + "WAVE".toByteArray())).isNull()
        assertThat(ImageFormat.detect(byteArrayOf())).isNull()
    }

}
