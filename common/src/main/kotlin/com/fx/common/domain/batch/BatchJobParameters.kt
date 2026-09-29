package com.fx.common.domain.batch

import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.ObjectNode

/**
 * 스케줄 · 수동 실행 요청의 `job_parameters` (문자열 값만 가진 JSON 객체).
 * 예: `{"topicType":"MAJOR"}`, 파라미터가 없으면 `{}`
 */
object BatchJobParameters {

    private val jsonMapper: JsonMapper = JsonMapper.builder().build()

    /** 실행마다 폴러가 붙이는 이름. 스케줄 · 요청 파라미터로는 쓸 수 없다. */
    val RESERVED_NAMES: Set<String> = setOf("scheduledAt", "requestId", "triggerType", "scheduleKey", "requestedBy")

    /** @throws IllegalArgumentException JSON 객체가 아니거나, 값이 문자열이 아니거나, 예약된 이름을 쓸 때 */
    fun parse(json: String): Map<String, String> {
        val node = try {
            jsonMapper.readTree(json)
        } catch (e: JacksonException) {
            throw IllegalArgumentException("Job 파라미터가 올바른 JSON 이 아닙니다: $json", e)
        }
        require(node is ObjectNode) { "Job 파라미터는 JSON 객체여야 합니다: $json" }

        return node.properties().associate { (name, value) ->
            require(value.isString) { "Job 파라미터 값은 문자열이어야 합니다: $name" }
            require(name !in RESERVED_NAMES) { "예약된 Job 파라미터 이름입니다: $name" }
            name to value.stringValue()
        }
    }

    fun format(parameters: Map<String, String>): String = jsonMapper.writeValueAsString(parameters)

}
