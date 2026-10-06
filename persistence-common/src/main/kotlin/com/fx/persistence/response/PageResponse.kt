package com.fx.persistence.response

import org.springframework.data.domain.Page

/**
 * 목록 조회 API 의 공통 페이지 응답 포맷.
 *
 * `currentPage` 는 클라이언트에게 노출되는 값으로 1-based 다.
 * 내부적으로 사용하는 Spring Data `Pageable`/`Page` 는 0-based 이므로,
 * 어댑터(컨트롤러) 경계에서 [from] 을 통해 변환한다.
 */
data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val hasNext: Boolean,
) {

    companion object {

        /**
         * [Page] 를 [PageResponse] 로 변환한다.
         * @param mapper 도메인 엔티티 → 응답 DTO 변환 함수
         */
        fun <T : Any, R> from(page: Page<T>, mapper: (T) -> R): PageResponse<R> =
            PageResponse(
                content = page.content.map(mapper),
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                currentPage = page.number + 1,
                hasNext = page.hasNext(),
            )

    }

}
