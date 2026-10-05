package com.fx.persistence

import java.time.LocalDateTime

/**
 * 단위 테스트에서 영속화하지 않은 엔티티에 식별자 · 감사 시각을 채운다.
 * [BaseEntity] 의 setter 가 protected 라 리플렉션으로 넣는다.
 */
fun <T : BaseEntity> T.withId(id: Long): T =
    apply {
        setBaseField("id", id)
    }

fun <T : BaseEntity> T.withAuditing(createdAt: LocalDateTime, updatedAt: LocalDateTime = createdAt): T =
    apply {
        setBaseField("createdAt", createdAt)
        setBaseField("updatedAt", updatedAt)
    }

private fun BaseEntity.setBaseField(name: String, value: Any) {
    BaseEntity::class.java.getDeclaredField(name).apply {
        isAccessible = true
    }.set(this, value)
}
