package com.fx.api.application.port.`in`

/**
 * 공지 알림 전송을 위한 Input Port 입니다.
 *
 * 특정 | 전체 사용자에게 공지 알림을 전송합니다.
 *
 * @author 이동섭
 * @since 2025-10-22
 */
interface NotificationUseCase {

    /**
     * 특정 사용자에게 공지 알림을 발송합니다.
     *
     * @param fcmToken 대상 사용자의 FCM 토큰
     * @param nttId 알림을 전송할 공지의 고유 ID
     * @return 알림 발송 성공 여부
     */
    fun notifyNotice(fcmToken: String, nttId: Long): Boolean

}