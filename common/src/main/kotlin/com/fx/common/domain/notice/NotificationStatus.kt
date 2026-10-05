package com.fx.common.domain.notice

/** 공지 알림 발송 상태. 배치가 재시작될 때 `PENDING` 부터 이어서 보낸다. */
enum class NotificationStatus {

    /** 발송 대기. 크롤링으로 새로 들어온 공지. */
    PENDING,

    /** 발송 완료. */
    SENT,

    /** 발송 대상 아님. 관리자가 직접 등록한 공지. */
    SKIPPED,

}
