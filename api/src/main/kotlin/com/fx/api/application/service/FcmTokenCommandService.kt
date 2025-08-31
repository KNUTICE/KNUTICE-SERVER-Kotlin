package com.fx.api.application.service

import com.fx.api.application.port.`in`.FcmTokenCommandUseCase
import com.fx.api.application.port.`in`.dto.FcmTokenCommand
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.global.domain.FcmToken
import org.springframework.stereotype.Service

@Service
class FcmTokenCommandService(
    private val fcmTokenPersistencePort: FcmTokenPersistencePort
): FcmTokenCommandUseCase {

    override fun saveFcmToken(fcmTokenCommand: FcmTokenCommand): Boolean {
        val fcmToken = fcmTokenPersistencePort.findByFcmToken(fcmTokenCommand.fcmToken)
            ?.copy(isActive = true) // 존재하는 경우 isActive = true
            ?: FcmToken.createFcmToken( // 없으면 새로 생성
                fcmToken = fcmTokenCommand.fcmToken,
                deviceType = fcmTokenCommand.deviceType
            )

        fcmTokenPersistencePort.saveFcmToken(fcmToken)
        return true
    }
}