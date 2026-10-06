package com.fx.persistence.response

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class PageResponseTest {

    @Test
    fun `Page 를 1부터 세는 페이지 번호로 바꾸고 항목을 변환한다`() {
        val page = PageImpl(listOf(1, 2), PageRequest.of(1, 2), 5)

        val response = PageResponse.from(page) {
            "item-$it"
        }

        assertThat(response).isEqualTo(
            PageResponse(
                content = listOf("item-1", "item-2"),
                totalElements = 5,
                totalPages = 3,
                currentPage = 2,
                hasNext = true,
            )
        )
    }

}
