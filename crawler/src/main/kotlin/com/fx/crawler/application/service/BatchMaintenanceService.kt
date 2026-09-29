package com.fx.crawler.application.service

import com.fx.crawler.application.port.`in`.BatchMaintenanceUseCase
import com.fx.crawler.application.port.out.BatchMetadataPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class BatchMaintenanceService(
    private val batchMetadataPort: BatchMetadataPort,
    private val clock: Clock,
) : BatchMaintenanceUseCase {

    @Transactional
    override fun deleteExpiredExecutions(retentionDays: Int, limit: Int): Int {
        require(retentionDays >= 1) { "보존 기간은 1일 이상이어야 합니다: $retentionDays" }
        val cutoff = LocalDateTime.now(clock).minusDays(retentionDays.toLong())
        return batchMetadataPort.deleteJobExecutionsEndedBefore(cutoff, limit)
    }

    @Transactional
    override fun deleteOrphanInstances(limit: Int): Int =
        batchMetadataPort.deleteOrphanJobInstances(limit)

}
