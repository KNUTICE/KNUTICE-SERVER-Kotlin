package com.fx.crawler.application.port.out

import com.fx.crawler.domain.embedding.EmbeddingInput
import com.fx.crawler.domain.embedding.EmbeddingUnavailableException

/**
 * 임베딩할 공지 본문 이미지를 학교 사이트에서 읽는다.
 * DB 에는 본문 첫 이미지 주소만 있으므로 상세 페이지를 다시 읽어 이미지 주소를 모두 얻는다.
 */
interface NoticeImagePort {

    /**
     * 지금 학교 사이트에 요청할 수 있는지 확인한다. 학교 사이트 장애로 멈춘 동안 본문 임베딩까지 해 놓고 버리지 않도록 먼저 부른다.
     * @throws EmbeddingUnavailableException 학교 사이트 장애로 요청을 멈춘 동안
     */
    fun checkAvailable()

    /**
     * 상세 페이지 본문의 이미지 주소. 본문에 나온 순서대로 돌려준다.
     * 허용하지 않은 사이트 · 없는 페이지(404 등 4xx) · 다른 곳으로 보내는 페이지(3xx)면 빈 목록이다.
     * @throws EmbeddingUnavailableException 학교 사이트 장애로 요청을 멈춘 동안 (공지 탓이 아니므로 미룬다)
     * @throws RuntimeException 학교 사이트 장애(429 · 5xx · 네트워크 오류)를 처음 만났을 때. 나중에 다시 시도할 수 있다
     */
    fun findImageUrls(contentUrl: String): List<String>

    /**
     * 이미지 원본. 받지 못한 이미지는 그 이미지만 건너뛰도록 null 이다:
     * 허용하지 않은 사이트 · 너무 작거나 큰 이미지 · 임베딩할 수 없는 형식 · 3xx · 4xx · 5xx · 네트워크 오류 · 시간 초과.
     */
    fun fetchImage(imageUrl: String): EmbeddingInput.Image?

}
