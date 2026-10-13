package com.fx.api.application.service.apilog

import com.fx.api.application.port.`in`.apilog.ApiLogCommandUseCase
import com.fx.api.application.port.`in`.apilog.dto.ApiLogCommand
import com.fx.api.application.port.out.apilog.ApiLogPersistencePort
import com.fx.api.application.port.out.fcmtoken.FcmTokenPersistencePort
import com.fx.api.domain.ApiLog
import com.fx.common.application.port.`in`.CatalogQueryUseCase
import com.fx.common.domain.catalog.TopicCatalog
import com.fx.common.domain.catalog.TopicView
import com.fx.common.domain.fcmtoken.FCM_TOKEN_MAX_LENGTH
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ApiLogCommandService(
    private val apiLogPersistencePort: ApiLogPersistencePort,
    private val fcmTokenPersistencePort: FcmTokenPersistencePort,
    private val catalogQueryUseCase: CatalogQueryUseCase,
) : ApiLogCommandUseCase {

    /** 묶음의 토큰은 한 번에 조회해 ID 로 바꾼다. 등록되지 않은 토큰 · 토픽은 비워 둔다. */
    @Transactional
    override fun saveAll(commands: List<ApiLogCommand>) {
        if (commands.isEmpty()) {
            return
        }
        val tokenIds = fcmTokenPersistencePort.findIdsByTokens(
            commands.mapNotNullTo(mutableSetOf()) {
                it.fcmToken?.takeIf { token ->
                    token.length <= FCM_TOKEN_MAX_LENGTH
                }
            }
        )
        val catalog = catalogQueryUseCase.getTopicCatalog()

        apiLogPersistencePort.saveAll(
            commands.map {
                ApiLog(
                    requestedAt = it.requestedAt,
                    method = it.method,
                    urlPattern = it.urlPattern,
                    statusCode = it.statusCode,
                    durationMs = it.durationMs,
                    topic = findTopic(catalog, it),
                    fcmTokenId = it.fcmToken?.let { token ->
                        tokenIds[token]
                    },
                )
            }
        )
    }

    /** v1 은 토픽 이름, v2 는 토픽 코드로 찾는다. 없거나 삭제된 토픽이면 null. */
    private fun findTopic(catalog: TopicCatalog, command: ApiLogCommand): TopicView? {
        command.topicName?.let {
            return catalog.findByName(it)
        }
        return command.topicId?.toIntOrNull()?.let {
            catalog.findByCode(it)
        }
    }

}
