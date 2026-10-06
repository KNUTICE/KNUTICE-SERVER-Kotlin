package com.fx.common.domain.batch

/**
 * crawler 가 실행하는 Job. [jobName] 이 `batch_schedule` · `batch_run_request` 의 `job_name` 값이다.
 *
 * Job 마다 받는 업무 파라미터가 정해져 있다. 관리자 API 는 저장하기 전에, crawler 는 실행하기 전에 [validate] 로 검증한다.
 */
enum class BatchJob(
    val jobName: String,
    /** 관리자 화면에 보여 줄 설명 */
    val description: String,
    val parameters: List<BatchJobParameter>,
) {

    NOTICE_CRAWL("noticeCrawlJob", "공지 · 학과 게시판 크롤링 · 알림", listOf(BatchJobParameter.TOPIC_TYPE)),
    NOTICE_SUMMARY("noticeSummaryJob", "요약 대기 공지 AI 요약", emptyList()),
    MEAL_NOTIFY("mealNotifyJob", "오늘 학식 알림", emptyList()),
    SILENT_PUSH("silentPushJob", "iOS 토큰 갱신용 사일런트 푸시", emptyList()),
    SEAT_ALERT_CHECK("seatAlertCheckJob", "열람실 빈자리 확인 · 알림, 만료 알림 정리", emptyList()),
    MAINTENANCE("maintenanceJob", "보존 기간이 지난 실행 기록 삭제", listOf(BatchJobParameter.RETENTION_DAYS)),
    ;

    /** @throws IllegalArgumentException 받지 않는 파라미터가 있거나, 필요한 파라미터가 없거나, 값이 잘못됐을 때 */
    fun validate(parameters: Map<String, String>) {
        val known = this.parameters.map {
            it.parameterName
        }
        val unknown = parameters.keys - known.toSet()
        require(unknown.isEmpty()) {
            "$jobName 에는 없는 파라미터입니다: ${unknown.joinToString()}"
        }

        this.parameters.forEach { parameter ->
            val value = requireNotNull(parameters[parameter.parameterName]) {
                "$jobName 에는 ${parameter.parameterName} 파라미터가 필요합니다."
            }
            parameter.validate(value)
        }
    }

    companion object {

        fun from(jobName: String): BatchJob? =
            entries.find {
                it.jobName == jobName
            }

    }

}
