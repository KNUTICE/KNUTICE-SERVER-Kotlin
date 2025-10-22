package com.fx.api.application.service

import com.fx.api.application.port.`in`.NotificationUseCase
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.application.port.out.NoticePersistencePort
import com.fx.api.application.port.out.NotificationWebPort
import com.fx.global.exception.FcmTokenException
import com.fx.global.exception.NoticeException
import com.fx.global.exception.errorcode.FcmTokenErrorCode
import com.fx.global.exception.errorcode.NoticeErrorCode
import org.springframework.stereotype.Service

@Service
class NotificationService(
    private val notificationWebPort: NotificationWebPort,
    private val fcmTokenPersistencePort: FcmTokenPersistencePort,
    private val noticePersistencePort: NoticePersistencePort
) : NotificationUseCase {

    /**
     * 공지 알림을 발송합니다.
     *
     * 1. FCM 토큰 존재 여부를 검증하고
     * 2. 공지 존재 여부를 확인한 뒤
     * 3. 외부 FCM 전송 포트를 호출합니다.
     *
     * @param fcmToken 대상 사용자의 FCM 토큰
     * @param nttId 알림을 전송할 공지의 고유 ID
     * @return 알림 발송 성공 여부
     * @throws FcmTokenException 유효하지 않은 토큰일 경우
     * @throws NoticeException 공지가 존재하지 않을 경우
     */
    override fun notifyNotice(fcmToken: String, nttId: Long): Boolean {
        if (!fcmTokenPersistencePort.existsByFcmToken(fcmToken)) {
            throw FcmTokenException(FcmTokenErrorCode.TOKEN_NOT_FOUND)
        }
        if (!noticePersistencePort.existsById(nttId)) {
            throw NoticeException(NoticeErrorCode.NOTICE_NOT_FOUND)
        }

        return notificationWebPort.notifyNotice(fcmToken, nttId)
    }

}