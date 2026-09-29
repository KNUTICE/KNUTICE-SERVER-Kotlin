package com.fx.crawler.domain.push

/**
 * 기기에 표시할 알림 하나. 앱은 받은 제목 · 본문을 그대로 표시한다.
 * @property data 앱이 알림을 눌렀을 때 쓰는 딥링크 등
 */
data class PushMessage(
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val data: Map<String, String> = emptyMap(),
)
