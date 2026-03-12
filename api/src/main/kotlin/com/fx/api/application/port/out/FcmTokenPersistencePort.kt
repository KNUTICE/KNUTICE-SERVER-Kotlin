package com.fx.api.application.port.out

import com.fx.api.application.port.out.dto.TopicUpdateQuery
import com.fx.global.domain.DeviceType
import com.fx.global.domain.FcmToken

interface FcmTokenPersistencePort {

    fun saveFcmToken(fcmToken: FcmToken)

    fun findByFcmToken(fcmToken: String): FcmToken?

    fun existsByFcmToken(fcmToken: String): Boolean

    fun countByIsActiveAndDeviceType(isActive: Boolean, deviceType: DeviceType): Long

    /**
     * MongoDB $addToSet / $pull 연산자로 토픽을 원자적으로 추가/제거
     * @return 대상 토큰이 존재하면 true, 없으면 false
     */
    fun atomicUpdateTopic(topicUpdateQuery: TopicUpdateQuery): Boolean

}