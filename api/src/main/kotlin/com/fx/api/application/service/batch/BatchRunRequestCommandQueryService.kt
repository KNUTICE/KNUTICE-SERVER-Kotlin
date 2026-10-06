package com.fx.api.application.service.batch

import com.fx.api.application.port.`in`.batch.BatchRunRequestCommandUseCase
import com.fx.api.application.port.`in`.batch.BatchRunRequestQueryUseCase
import com.fx.api.application.port.`in`.batch.dto.BatchRunRequestCommand
import com.fx.api.application.port.`in`.batch.dto.BatchRunRequestSearchCommand
import com.fx.api.application.port.out.batch.BatchRunRequestPersistencePort
import com.fx.api.application.port.out.user.UserPersistencePort
import com.fx.api.domain.CursorPage
import com.fx.api.exception.BatchException
import com.fx.api.exception.errorcode.BatchErrorCode
import com.fx.common.domain.batch.BatchJobParameters
import com.fx.common.domain.batch.BatchRunRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Job 수동 실행 요청 · 조회.
 *
 * 요청은 저장만 하고, crawler 의 폴러가 다음 확인 때(최대 1분 뒤) 실행하거나 거절한다.
 * 같은 작업이 이미 실행 중인지는 crawler 만 알므로 그 경우는 crawler 가 거절(REJECTED)한다.
 */
@Service
@Transactional(readOnly = true)
class BatchRunRequestCommandQueryService(
    private val batchRunRequestPersistencePort: BatchRunRequestPersistencePort,
    private val userPersistencePort: UserPersistencePort,
) : BatchRunRequestCommandUseCase, BatchRunRequestQueryUseCase {

    @Transactional
    override fun requestRun(command: BatchRunRequestCommand): BatchRunRequest {
        val jobParameters = BatchInputValidator.jobParameters(command.jobName, command.jobParameters)
        // 실행 버튼을 연달아 눌러 같은 작업이 여러 번 쌓이지 않게 한다
        val duplicated = batchRunRequestPersistencePort.findRequested(command.jobName).any {
            BatchJobParameters.parse(it.jobParameters) == command.jobParameters
        }
        if (duplicated) {
            throw BatchException(BatchErrorCode.RUN_REQUEST_DUPLICATED)
        }

        val requester = userPersistencePort.getById(command.userId)
        return batchRunRequestPersistencePort.save(BatchRunRequest(command.jobName, jobParameters, requester.nickname))
    }

    override fun getRunRequests(command: BatchRunRequestSearchCommand): CursorPage<BatchRunRequest> {
        val rows = batchRunRequestPersistencePort.findRequests(command.status, command.cursor, command.size + 1)
        return CursorPage.of(rows, command.size) {
            requireNotNull(it.id)
        }
    }

}
