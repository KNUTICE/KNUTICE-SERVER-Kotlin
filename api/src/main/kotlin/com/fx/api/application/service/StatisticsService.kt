package com.fx.api.application.service

import com.fx.api.application.port.`in`.StatisticsQueryUseCase
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.application.port.out.NoticePersistencePort
import com.fx.api.domain.Statistics
import com.fx.global.domain.DeviceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service

@Service
class StatisticsService(
    private val noticePersistencePort: NoticePersistencePort,
    private val fcmTokenPersistencePort: FcmTokenPersistencePort
) : StatisticsQueryUseCase {

    /**
     * 비동기 병렬처리
     */
    override suspend fun getStatistics(): Statistics = coroutineScope {
        // 공지 통계
        val noticeCount = async(Dispatchers.IO) { noticePersistencePort.count() }
        val summaryContentCount = async(Dispatchers.IO) { noticePersistencePort.countByContentSummaryExists(true) }

        // 토큰 통계
        val fcmTokenCount = async(Dispatchers.IO) { fcmTokenPersistencePort.count() }
        val aosActiveFcmTokenCount = async(Dispatchers.IO) { fcmTokenPersistencePort.countByIsActiveTrueAndDeviceType(DeviceType.AOS) }
        val iosActiveFcmTokenCount = async(Dispatchers.IO) { fcmTokenPersistencePort.countByIsActiveTrueAndDeviceType(DeviceType.iOS) }

        Statistics(
            noticeCount = noticeCount.await(),
            summaryContentCount = summaryContentCount.await(),
            fcmTokenCount = fcmTokenCount.await(),
            aosActiveFcmTokenCount = aosActiveFcmTokenCount.await(),
            iosActiveFcmTokenCount = iosActiveFcmTokenCount.await()
        )
    }

}
