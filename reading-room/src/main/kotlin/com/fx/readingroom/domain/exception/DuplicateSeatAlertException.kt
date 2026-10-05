package com.fx.readingroom.domain.exception

/** 같은 토큰 · 열람실 · 좌석의 알림이 이미 있다. 동시 요청이 유니크 제약에 걸린 경우 어댑터가 번역해 던진다. */
class DuplicateSeatAlertException(cause: Throwable? = null) : RuntimeException(cause)
