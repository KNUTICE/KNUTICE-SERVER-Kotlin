package com.fx.persistence.request

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PagingRequestTest {

    @Test
    fun `1부터 세는 페이지 번호를 0부터 세는 Pageable 로 바꾼다`() {
        val pageable = PagingRequest(page = 3, size = 10).toPageable()

        assertThat(pageable.pageNumber).isEqualTo(2)
        assertThat(pageable.pageSize).isEqualTo(10)
        assertThat(pageable.offset).isEqualTo(20)
    }

    @Test
    fun `보내지 않으면 첫 페이지를 20개씩 읽는다`() {
        val pageable = PagingRequest().toPageable()

        assertThat(pageable.pageNumber).isZero()
        assertThat(pageable.pageSize).isEqualTo(20)
    }

}
