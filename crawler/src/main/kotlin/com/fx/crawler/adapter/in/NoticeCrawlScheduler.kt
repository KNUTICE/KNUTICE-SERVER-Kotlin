package com.fx.crawler.adapter.`in`

import com.fx.crawler.appllication.port.`in`.NoticeCrawlUseCase
import com.fx.crawler.appllication.port.`in`.NotificationUseCase
import com.fx.crawler.common.annotation.ScheduleAdapter
import com.fx.global.domain.CrawlableType
import com.fx.global.domain.MajorType
import com.fx.global.domain.NoticeType
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled

@ScheduleAdapter
class NoticeCrawlScheduler(
    private val noticeCrawlUseCase: NoticeCrawlUseCase,
    private val notificationUseCase: NotificationUseCase
) {

    private val log = LoggerFactory.getLogger(NoticeCrawlScheduler::class.java)


    @Scheduled(cron = "0 0/15 * * * *") // 매일 15분마다
    fun crawlAndPushNotices() = runBlocking {
        processCrawl(NoticeType.entries, "notice")
    }

    @Scheduled(cron = "0 10 16 * * *") // 매일 16시 10분마다
    fun crawlAndPushMajorNotices() = runBlocking {
        processCrawl(MajorType.entries, "major notice")
    }

    private suspend fun processCrawl(targets: List<CrawlableType>, label: String) {
        log.info("---------- Starting scheduled $label crawling ----------")
        try {
            val newNotices = noticeCrawlUseCase.crawlAndSaveNotices(targets)
            log.info("---------- Scheduled crawl finished ----------")
            if (newNotices.isEmpty()) return

            newNotices.forEach {
                log.info("New $label found - nttId: {}, title: {}", it.nttId, it.title)
            }

            log.info("---------- Starting notification ----------")
            notificationUseCase.sendNotification(newNotices)
            log.info("---------- Notification completed ----------")
        } catch (e: Exception) {
            log.error("Error during scheduled $label crawling", e)
        }
    }

}