package com.fx.api.application.service

import com.fx.api.application.port.`in`.FcmTokenQueryUseCase
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.exception.FcmTokenException
import com.fx.api.exception.errorcode.FcmTokenBaseErrorCode
import com.fx.global.domain.FcmToken
import org.springframework.stereotype.Service

@Service
class FcmTokenQueryService(
    private val fcmTokenPersistencePort: FcmTokenPersistencePort
): FcmTokenQueryUseCase {

    override fun getMyTokenInfo(fcmToken: String): FcmToken =
        fcmTokenPersistencePort.findByFcmToken(fcmToken)?: throw FcmTokenException(FcmTokenBaseErrorCode.TOKEN_NOT_FOUND)

}