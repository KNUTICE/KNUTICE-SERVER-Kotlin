package com.fx.api.application.service

import com.fx.api.application.port.`in`.ApiLogCommandUseCase
import com.fx.api.application.port.`in`.dto.ApiLogSaveCommand
import com.fx.api.application.port.out.ApiLogPersistencePort
import com.fx.api.domain.ApiLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service

@Service
class ApiLogCommandService(
    private val apiLogPersistencePort: ApiLogPersistencePort
) : ApiLogCommandUseCase {

    /**
     * dslee (2026-02-07) : API 로그 기록을 비동기로 처리하기 위한 코루틴 스코프
     * SupervisorJob 을 사용해 개별 작업 실패가 전체 스코프에 영향을 미치지 않도록 함
     */
    private val logScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun recordApiLog(apiLogSaveCommand: ApiLogSaveCommand) {
        logScope.launch {
            apiLogPersistencePort.save(ApiLog(
                urlPattern = apiLogSaveCommand.urlPattern,
                url = apiLogSaveCommand.url,
                method = apiLogSaveCommand.method,
                queryParameters = apiLogSaveCommand.queryParameters,
                fcmToken = apiLogSaveCommand.fcmToken,
                clientIp = apiLogSaveCommand.clientIp,
                deviceType = apiLogSaveCommand.deviceType,
                statusCode = apiLogSaveCommand.statusCode,
                executionTime = apiLogSaveCommand.executionTime
            ))
        }
    }

}