package com.fx.crawler.application.port.`in`

/** 스케줄 · 수동 요청에 따른 Job 실행. 폴러가 1분마다 부른다. */
interface BatchTriggerUseCase {

    /** 발화 시각이 된 스케줄을 실행한다. */
    fun triggerDueSchedules()

    /** 쌓인 수동 실행 요청을 실행한다. */
    fun processRunRequests()

    /** 비정상 종료로 실행 중에 멈춘 Job 을 실패로 정리한다. 기동할 때 한 번 부른다. */
    fun recoverInterruptedJobs()

}
