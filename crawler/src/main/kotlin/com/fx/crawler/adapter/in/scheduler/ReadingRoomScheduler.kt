package com.fx.crawler.adapter.`in`.scheduler

import com.fx.crawler.appllication.port.`in`.readingroom.SeatAlertProcessUseCase
import com.fx.crawler.common.annotation.ScheduleAdapter
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled

@ScheduleAdapter
class ReadingRoomScheduler(
    private val seatAlertProcessUseCase: SeatAlertProcessUseCase
) {

    private val log = LoggerFactory.getLogger(ReadingRoomScheduler::class.java)

    @Scheduled(cron = "0 */1 * * * *") // 매 1분마다
    fun checkAndPushSeats() = runBlocking {
        log.info("---------- Starting scheduled seat push notification ----------")
        seatAlertProcessUseCase.checkAndNotifyAll()
        log.info("---------- Scheduled seat notification finished ----------")
    }

}