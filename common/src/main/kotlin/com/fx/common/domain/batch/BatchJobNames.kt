package com.fx.common.domain.batch

/** crawler 가 실행하는 Job 이름. `batch_schedule` · `batch_run_request` 의 `job_name` 값이다. */
object BatchJobNames {

    const val NOTICE_CRAWL = "noticeCrawlJob"
    const val NOTICE_SUMMARY = "noticeSummaryJob"
    const val MEAL_NOTIFY = "mealNotifyJob"
    const val SILENT_PUSH = "silentPushJob"
    const val SEAT_ALERT_CHECK = "seatAlertCheckJob"
    const val MAINTENANCE = "maintenanceJob"

}
