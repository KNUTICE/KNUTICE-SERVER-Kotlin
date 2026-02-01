package com.fx.crawler.appllication.service

import com.fx.crawler.appllication.port.`in`.StatisticsUseCase
import com.fx.crawler.appllication.port.out.FcmTokenPersistencePort
import com.fx.crawler.appllication.port.out.NoticePersistencePort
import com.fx.crawler.appllication.port.out.StatisticsPersistencePort
import com.fx.global.domain.DailyStatistics
import com.fx.global.domain.DeviceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalTime

@Service
class StatisticsService(
    private val statisticsPersistencePort: StatisticsPersistencePort,
    private val noticePersistencePort: NoticePersistencePort,
    private val fcmTokenPersistencePort: FcmTokenPersistencePort
) : StatisticsUseCase {

    private val log = LoggerFactory.getLogger(StatisticsService::class.java)

    /**
     * 전날(Yesterday)의 활동 및 사용자 현황 데이터를 집계하여 일일 통계를 생성하고 저장합니다.
     *
     * [성능 최적화]
     * Spring Data MongoDB (Blocking Driver) 환경에서 전체 집계 시간을 단축하기 위해
     * [Dispatchers.IO]와 [async]를 사용하여 6개의 쿼리를 병렬로 실행합니다.
     * **미지정 시 스케줄러 쓰레드 하나에서 순차적으로 실행되어 성능 저하가 발생할 수 있습니다.**
     */
    override suspend fun aggregateDailyStatistics() = withContext(Dispatchers.IO) {
        val yesterday = LocalDate.now().minusDays(1)
        val endOfDay = yesterday.atTime(LocalTime.MAX)

        val noticeCount = async {
            noticePersistencePort.countByCreatedAtLessThanEqual(endOfDay)
        }
        val noticeSummaryCount = async {
            noticePersistencePort.countByCreatedAtLessThanEqualAndHasSummary(endOfDay)
        }

        // 2. FCM 토큰 데이터 집계 (현재 시점의 누적 상태)
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

        statisticsPersistencePort.save(DailyStatistics(
            statisticsDate = yesterday,
            noticeCount = noticeCount.await(),
            noticeSummaryCount = noticeSummaryCount.await(),

            fcmTokenActiveAosCount = activeAos.await(),
            fcmTokenActiveIosCount = activeIos.await(),
            fcmTokenActiveCount = activeAos.await() + activeIos.await(),

            fcmTokenInactiveAosCount = inactiveAos.await(),
            fcmTokenInactiveIosCount = inactiveIos.await(),
            fcmTokenInactiveCount = inactiveAos.await() + inactiveIos.await()
        ))
    }

}