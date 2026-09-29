package com.fx.crawler.adapter.`in`.batch.push

import com.fx.crawler.application.port.`in`.NoticePushUseCase
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.listener.StepExecutionListener
import org.springframework.batch.core.step.StepExecution
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue

/**
 * 토픽 파티션 발송이 끝나면 그 토픽의 공지만 발송 완료로 표시한다.
 * 한 토픽이 실패해도 다른 토픽은 완료로 남고, 실패한 토픽의 공지는 다음 실행에서 다시 보낸다.
 */
@Component
class NoticeNotifiedListener(
    private val noticePushUseCase: NoticePushUseCase,
    private val jsonMapper: JsonMapper,
) : StepExecutionListener {

    override fun afterStep(stepExecution: StepExecution): ExitStatus {
        val context = stepExecution.executionContext
        if (stepExecution.status == BatchStatus.COMPLETED && context.containsKey(PushPartitionKeys.NOTICE_IDS)) {
            noticePushUseCase.markNotified(jsonMapper.readValue<List<Long>>(context.getString(PushPartitionKeys.NOTICE_IDS)))
        }
        return stepExecution.exitStatus
    }

}
