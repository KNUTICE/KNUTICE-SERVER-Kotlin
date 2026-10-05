package com.fx.crawler.application.port.`in`

interface BatchMaintenanceUseCase {

    /** 보존 기간이 지난 JobExecution 을 [limit] 개까지 지우고 지운 수를 돌려준다. */
    fun deleteExpiredExecutions(retentionDays: Int, limit: Int): Int

    /** JobExecution 이 남지 않은 JobInstance 를 [limit] 개까지 지운다. */
    fun deleteOrphanInstances(limit: Int): Int

}
