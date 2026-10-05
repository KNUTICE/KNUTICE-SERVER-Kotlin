package com.fx.crawler.application.port.out

import java.time.LocalDateTime

/** Spring Batch 메타데이터(`BATCH_*`) 정리. */
interface BatchMetadataPort {

    /** [cutoff] 이전에 끝난 JobExecution 을 [limit] 개까지 딸린 행과 함께 지우고 지운 수를 돌려준다. */
    fun deleteJobExecutionsEndedBefore(cutoff: LocalDateTime, limit: Int): Int

    /** JobExecution 이 하나도 남지 않은 JobInstance 를 [limit] 개까지 지운다. */
    fun deleteOrphanJobInstances(limit: Int): Int

}
