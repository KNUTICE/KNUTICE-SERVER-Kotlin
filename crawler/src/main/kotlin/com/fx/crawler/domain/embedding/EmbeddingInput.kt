package com.fx.crawler.domain.embedding

/** 임베딩 모델에 보내는 입력 하나. 텍스트와 이미지는 같은 벡터 공간에 놓인다. */
sealed interface EmbeddingInput {

    data class Text(val text: String) : EmbeddingInput

    /** 받아 온 이미지 원본. 바이트 배열이라 내용 비교를 하지 않으므로 data class 로 두지 않는다. */
    class Image(val bytes: ByteArray, val mimeType: String) : EmbeddingInput {

        override fun toString(): String =
            "Image(mimeType=$mimeType, bytes=${bytes.size})"

    }

}
