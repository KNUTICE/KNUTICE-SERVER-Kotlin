package com.fx.crawler.adapter.`in`.scheduler

import com.fx.crawler.appllication.port.`in`.NotificationUseCase
import com.fx.crawler.common.annotation.ScheduleAdapter
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled

@ScheduleAdapter
class SilentPushScheduler(
    private val notificationUseCase: NotificationUseCase
) {

    private val log = LoggerFactory.getLogger(SilentPushScheduler::class.java)

    @Scheduled(cron = "0 0 0 1 * *")
    fun sendSilentPushNotification() = runBlocking{
        log.info("---------- Starting scheduled silent push notification ----------")
        notificationUseCase.sendSilentPushNotification()
        log.info("---------- Scheduled silent push notification finished ----------")
    }

}