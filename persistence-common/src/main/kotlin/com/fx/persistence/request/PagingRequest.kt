package com.fx.persistence.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.PageRequest as SpringPageRequest

/**
 * 목록 조회 API 의 공통 페이지 요청 파라미터.
 *
 * [page] 는 클라이언트가 전달하는 1-based 값이다. Spring Data `Pageable` 은 0-based 이므로
 * [toPageable] 경계에서 변환한다. (응답 변환은 [com.fx.persistence.response.PageResponse])
 *
 * 검증은 어댑터에서 `@Valid` 로 활성화한다. 실패 시 발생하는 `MethodArgumentNotValidException` 을
 * `GlobalExceptionHandler` 가 공통 400 응답으로 변환한다.
 * - [page] : 1 이상. `page = 0` 이면 `page - 1` 이 음수가 되어 `PageRequest` 계약을 위반하므로 요청 단계에서 막는다.
 * - [size] : 1 이상 [MAX_SIZE] 이하. 상한이 없으면 외부에서 `size = Int.MAX_VALUE` 를 넘겨
 *   과도한 DB 조회와 메모리 할당을 유발할 수 있다 (CWE-770).
 */
data class PagingRequest(
    @field:Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
    val page: Int = DEFAULT_PAGE,

    @field:Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @field:Max(value = MAX_SIZE, message = "페이지 크기는 100 이하여야 합니다.")
    val size: Int = DEFAULT_SIZE,
) {

    /** 1-based [page] 를 0-based Spring [Pageable] 로 변환한다. */
    fun toPageable(): Pageable =
        SpringPageRequest.of(page - 1, size)

    companion object {
        const val DEFAULT_PAGE: Int = 1
        const val DEFAULT_SIZE: Int = 20
        const val MAX_SIZE: Long = 100
    }

}
