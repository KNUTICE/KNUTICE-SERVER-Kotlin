package com.fx.api.application.port.out

import com.fx.api.application.port.out.dto.TopicUpdateQuery
import com.fx.global.domain.DeviceType
import com.fx.global.domain.FcmToken

interface FcmTokenPersistencePort {

    suspend fun saveFcmToken(fcmToken: FcmToken)

    suspend fun findByFcmToken(fcmToken: String): FcmToken?

    suspend fun existsByFcmToken(fcmToken: String): Boolean

    suspend fun countByIsActiveAndDeviceType(isActive: Boolean, deviceType: DeviceType): Long

    /**
     * MongoDB $addToSet / $pull 연산자로 토픽을 원자적으로 추가/제거
     * @return 대상 토큰이 존재하면 true, 없으면 false
     */
    suspend fun atomicUpdateTopic(topicUpdateQuery: TopicUpdateQuery): Boolean

}