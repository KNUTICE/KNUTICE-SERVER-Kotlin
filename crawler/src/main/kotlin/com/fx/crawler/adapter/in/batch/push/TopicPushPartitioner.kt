package com.fx.crawler.adapter.`in`.batch.push

import com.fx.crawler.domain.push.TopicPushPlan
import org.springframework.batch.core.partition.Partitioner
import org.springframework.batch.infrastructure.item.ExecutionContext
import tools.jackson.databind.json.JsonMapper

/**
 * 토픽별 발송 계획을 파티션으로 나눈다. 알림 문구는 파티션을 나눌 때 만들어 ExecutionContext 에 담으므로
 * 발송 Step 은 공지 · 학식 여부를 모르고 받은 알림만 보낸다. 보낼 토픽이 없으면 파티션도 없다.
 */
class TopicPushPartitioner(
    private val jsonMapper: JsonMapper,
    private val plans: () -> List<TopicPushPlan>,
) : Partitioner {

    override fun partition(gridSize: Int): Map<String, ExecutionContext> =
        plans().associate { plan ->
            "topic-${plan.topicCode}" to ExecutionContext().apply {
                putInt(PushPartitionKeys.TOPIC_CODE, plan.topicCode)
                putString(PushPartitionKeys.MESSAGES, jsonMapper.writeValueAsString(plan.messages))
                putString(PushPartitionKeys.NOTICE_IDS, jsonMapper.writeValueAsString(plan.noticeIds))
            }
        }

}
