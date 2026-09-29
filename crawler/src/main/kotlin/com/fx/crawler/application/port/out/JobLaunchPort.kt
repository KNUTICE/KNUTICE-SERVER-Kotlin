package com.fx.crawler.application.port.out

import com.fx.crawler.domain.batch.JobLaunchRequest

/** Spring Batch Job 실행. */
interface JobLaunchPort {

    fun exists(jobName: String): Boolean

    /** 같은 Job 이 같은 업무 파라미터로 실행 중인지. 실행마다 달라지는 파라미터(발화 시각 등)는 비교하지 않는다. */
    fun isRunning(jobName: String, parameters: Map<String, String>): Boolean

    /** Job 을 비동기로 시작하고 JobExecution ID 를 돌려준다. */
    fun launch(request: JobLaunchRequest): Long

    /**
     * 프로세스가 비정상 종료돼 실행 중으로 남은 JobExecution 을 실패로 바꾼다.
     * 기동 직후(아직 아무 Job 도 실행하지 않았을 때)에만 부른다. 복구한 JobExecution ID 를 돌려준다.
     */
    fun recoverInterrupted(): List<Long>

}
