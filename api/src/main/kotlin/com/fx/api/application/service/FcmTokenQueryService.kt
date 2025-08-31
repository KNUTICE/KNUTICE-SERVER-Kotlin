package com.fx.api.application.service

import com.fx.api.application.port.`in`.FcmTokenQueryUseCase
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.global.domain.FcmToken
import org.springframework.stereotype.Service
import java.lang.RuntimeException

@Service
class FcmTokenQueryService(
    private val fcmTokenPersistencePort: FcmTokenPersistencePort
): FcmTokenQueryUseCase {

    override fun getMyTokenInfo(fcmToken: String): FcmToken =
        fcmTokenPersistencePort.findByFcmToken(fcmToken)?: throw RuntimeException("토큰 없음")

}