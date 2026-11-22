package com.fx.crawler.domain.embedding

/**
 * 임베딩 모델이 받는 이미지 형식. 학교 사이트의 Content-Type 을 믿지 않고 파일 앞부분(매직 넘버)으로 판단한다.
 * GIF · BMP 등 모델이 받지 않는 형식은 [detect] 가 null 을 돌려준다.
 */
enum class ImageFormat(val mimeType: String) {

    PNG("image/png"),
    JPEG("image/jpeg"),
    WEBP("image/webp"),
    ;

    companion object {

        private val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        private val JPEG_SIGNATURE = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
        private val RIFF = "RIFF".toByteArray(Charsets.US_ASCII)
        private val WEBP_MARK = "WEBP".toByteArray(Charsets.US_ASCII)

        fun detect(bytes: ByteArray): ImageFormat? =
            when {
                bytes.startsWith(PNG_SIGNATURE) -> PNG
                bytes.startsWith(JPEG_SIGNATURE) -> JPEG
                bytes.startsWith(RIFF) && bytes.startsWith(WEBP_MARK, offset = 8) -> WEBP
                else -> null
            }

        private fun ByteArray.startsWith(prefix: ByteArray, offset: Int = 0): Boolean =
            size >= offset + prefix.size && prefix.indices.all {
                this[offset + it] == prefix[it]
            }

    }

}
