package com.fx.api.application.service

import com.fx.api.application.port.`in`.StatisticsQueryUseCase
import com.fx.api.application.port.out.FcmTokenPersistencePort
import com.fx.api.application.port.out.NoticePersistencePort
import com.fx.api.application.port.out.StatisticsPersistencePort
import com.fx.global.domain.DailyStatistics
import com.fx.global.domain.DeviceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class StatisticsService(
    private val statisticsPersistencePort: StatisticsPersistencePort,
    private val noticePersistencePort: NoticePersistencePort,
    private val fcmTokenPersistencePort: FcmTokenPersistencePort
) : StatisticsQueryUseCase {

    override suspend fun getDailyStatistics(cursorDate: LocalDate?, size: Int): List<DailyStatistics> {
        val today = LocalDate.now()
        val resultList = mutableListOf<DailyStatistics>()

        // 1. 커서가 없으면 오늘 날짜 통계 계산 후 추가
        if (cursorDate == null) {
            resultList.add(calculateTodayStatistics(today))
        }

        // 2. DB 조회 기준 날짜 결정
        // 커서가 없으면 오늘(today) 미만인 데이터를 조회하면 되고,
        // 커서가 있으면 그 커서(날짜) 미만인 데이터를 조회하면 중복이 없음
        val startingCursor = cursorDate ?: today
        val querySize = size - resultList.size

        if (querySize > 0) {
            val pageable = PageRequest.of(0, querySize, Sort.by(Sort.Direction.DESC, "statisticsDate"))
            val dailyStatistics = statisticsPersistencePort.findAllByDateLessThan(startingCursor, pageable)
            resultList.addAll(dailyStatistics)
        }

        return resultList
    }

    private suspend fun calculateTodayStatistics(today: LocalDate): DailyStatistics = withContext(Dispatchers.IO) {
        val startOfDay = today.atStartOfDay()
        val endOfDay = LocalDateTime.now() // 현재 시간까지만

        val noticeCount = async {
            noticePersistencePort.countByCreatedAtBetween(startOfDay, endOfDay)
        }
        val noticeSummaryCount = async {
            noticePersistencePort.countByCreatedAtBetweenAndHasSummary(startOfDay, endOfDay)
        }

        val activeAos = async {
            fcmTokenPersistencePort.countByIsActiveAndDeviceType(true, DeviceType.AOS)
        }
        val activeIos = async {
            fcmTokenPersistencePort.countByIsActiveAndDeviceType(true, DeviceType.iOS)
        }
        val inactiveAos = async {
            fcmTokenPersistencePort.countByIsActiveAndDeviceType(false, DeviceType.AOS)
        }
        val inactiveIos = async {
            fcmTokenPersistencePort.countByIsActiveAndDeviceType(false, DeviceType.iOS)
        }

        DailyStatistics(
            statisticsDate = today,
            noticeCount = noticeCount.await(),
            noticeSummaryCount = noticeSummaryCount.await(),
            fcmTokenActiveAosCount = activeAos.await(),
            fcmTokenActiveIosCount = activeIos.await(),
            fcmTokenActiveCount = activeAos.await() + activeIos.await(),
            fcmTokenInactiveAosCount = inactiveAos.await(),
            fcmTokenInactiveIosCount = inactiveIos.await(),
            fcmTokenInactiveCount = inactiveAos.await() + inactiveIos.await()
        )
    }

}