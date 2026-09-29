package com.fx.crawler.adapter.`in`.batch.support

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.batch.infrastructure.item.ExecutionContext

class KeysetItemReaderTest {

    private val rows = (1L..5L).toList()
    private val requestedKeys = mutableListOf<Long?>()

    private fun reader() = KeysetItemReader<Long>("test", pageSize = 2, keyOf = { it }) { afterKey, size ->
        requestedKeys += afterKey
        rows.filter { afterKey == null || it > afterKey }.take(size)
    }

    private fun readAll(reader: KeysetItemReader<Long>): List<Long> = generateSequence { reader.read() }.toList()

    @Test
    fun `마지막 키 다음부터 한 페이지씩 읽는다`() {
        val reader = reader().apply { open(ExecutionContext()) }

        assertThat(readAll(reader)).containsExactly(1L, 2L, 3L, 4L, 5L)
        // 마지막 페이지가 페이지 크기보다 작으면 더 읽지 않는다
        assertThat(requestedKeys).containsExactly(null, 2L, 4L)
    }

    @Test
    fun `읽은 마지막 키를 저장하고 재시작하면 그 다음부터 읽는다`() {
        val context = ExecutionContext()
        val first = reader().apply { open(context) }
        first.read()
        first.read()
        first.read()
        first.update(context)

        assertThat(context.getLong("test.lastKey")).isEqualTo(3L)

        val restarted = reader().apply { open(context) }
        assertThat(readAll(restarted)).containsExactly(4L, 5L)
    }

    @Test
    fun `읽는 도중 조건에서 빠진 행이 있어도 건너뛰지 않는다`() {
        val remaining = rows.toMutableList()
        val reader = KeysetItemReader<Long>("test", pageSize = 2, keyOf = { it }) { afterKey, size ->
            remaining.filter { afterKey == null || it > afterKey }.take(size)
        }.apply { open(ExecutionContext()) }

        val read = mutableListOf<Long>()
        generateSequence { reader.read() }.forEach {
            read += it
            // 처리한 행은 조회 조건(예: 상태)에서 빠진다
            remaining.remove(it)
        }

        assertThat(read).containsExactly(1L, 2L, 3L, 4L, 5L)
    }

}
