package com.fx.api.domain

/** keyset 페이지. [nextCursor] 는 다음 페이지를 읽을 때 보낼 커서이고, 마지막 페이지면 null 이다. */
data class CursorPage<T>(
    val items: List<T>,
    val nextCursor: Long?,
) {

    companion object {

        /** [rows] 는 [size] 보다 하나 더 읽은 결과다. 하나가 더 있으면 다음 페이지가 있다. */
        fun <T> of(rows: List<T>, size: Int, cursorOf: (T) -> Long): CursorPage<T> {
            val items = rows.take(size)
            val nextCursor = if (rows.size > size) cursorOf(items.last()) else null
            return CursorPage(items, nextCursor)
        }

    }

}
