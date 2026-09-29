package com.fx.crawler.adapter.`in`.batch.support

import org.springframework.batch.infrastructure.item.ExecutionContext
import org.springframework.batch.infrastructure.item.ItemStreamReader

/**
 * 키(보통 id) 오름차순으로 한 페이지씩 읽는 reader. OFFSET 대신 `키 > 마지막 키` 로 다음 페이지를 읽는다.
 *
 * - 읽는 도중 조회 조건에서 빠지는 행(상태가 바뀐 행 등)이 있어도 행을 건너뛰거나 두 번 읽지 않는다.
 * - 커밋된 chunk 의 마지막 키를 ExecutionContext 에 저장하므로 재시작하면 그 다음부터 읽는다.
 * - 상태를 가지므로 Step 실행마다 새로 만든다 (`@StepScope`).
 *
 * @param fetchPage `(마지막 키, 페이지 크기)` 로 다음 페이지를 키 오름차순으로 읽는다. 첫 페이지는 마지막 키가 null 이다
 */
class KeysetItemReader<T : Any>(
    name: String,
    private val pageSize: Int,
    private val keyOf: (T) -> Long,
    private val fetchPage: (afterKey: Long?, size: Int) -> List<T>,
) : ItemStreamReader<T> {

    private val lastKeyName = "$name.lastKey"
    private val buffer = ArrayDeque<T>()
    private var lastFetchedKey: Long? = null
    private var lastReadKey: Long? = null
    private var exhausted = false

    init {
        require(pageSize > 0) { "페이지 크기는 1 이상이어야 합니다." }
    }

    override fun open(executionContext: ExecutionContext) {
        buffer.clear()
        exhausted = false
        lastReadKey = if (executionContext.containsKey(lastKeyName)) executionContext.getLong(lastKeyName) else null
        lastFetchedKey = lastReadKey
    }

    override fun read(): T? {
        if (buffer.isEmpty() && !exhausted) {
            fetchNextPage()
        }
        val item = buffer.removeFirstOrNull() ?: return null
        lastReadKey = keyOf(item)
        return item
    }

    override fun update(executionContext: ExecutionContext) {
        lastReadKey?.let { executionContext.putLong(lastKeyName, it) }
    }

    private fun fetchNextPage() {
        val page = fetchPage(lastFetchedKey, pageSize)
        if (page.size < pageSize) {
            exhausted = true
        }
        page.lastOrNull()?.let { lastFetchedKey = keyOf(it) }
        buffer.addAll(page)
    }

}
