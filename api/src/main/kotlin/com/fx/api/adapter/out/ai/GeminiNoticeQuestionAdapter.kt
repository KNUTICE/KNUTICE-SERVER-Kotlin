package com.fx.api.adapter.out.ai

import com.fx.api.application.port.out.question.QuestionEmbeddingPort
import com.fx.api.config.question.NoticeQuestionProperties
import com.fx.api.exception.QuestionException
import com.fx.api.exception.errorcode.QuestionErrorCode
import com.fx.common.annotation.hexagonal.WebOutputAdapter
import com.fx.common.domain.notice.EMBEDDING_DIMENSIONS
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import tools.jackson.databind.JsonNode
import kotlin.math.sqrt

/**
 * Gemini 기본 API 로 질문을 임베딩한다 (서버 무료 키). 답변은 서버가 만들지 않는다 (사용자 개인 키로 앱 · 웹이 직접 부른다).
 *
 * - 임베딩은 crawler 의 공지 임베딩과 같은 요청(`batchEmbedContents`, 768차원, taskType 없음)을 보내고 길이 1 로 정규화한다.
 *   모델 · 차원 · 정규화가 다르면 공지 벡터와 비교할 수 없다.
 * - 키는 `notice-question.api-key` (무료 등급 키만). 비어 있으면 질문 API 만 503 으로 막고 나머지 API 는 그대로 뜬다.
 * - 429 는 [QuestionErrorCode.AI_RATE_LIMITED], 그 밖의 거절 · 서버 · 네트워크 오류는 [QuestionErrorCode.AI_UNAVAILABLE].
 *   다시 보내면 한도만 더 쓰므로 재시도하지 않는다. 오류 응답 본문 · 키는 로그에 남기지 않는다.
 * - 읽기 제한 시간은 `spring.http.clients.read-timeout` 을 따른다.
 */
@WebOutputAdapter
class GeminiNoticeQuestionAdapter(
    restClientBuilder: RestClient.Builder,
    @Value("\${notice-question.api-key:}") private val apiKey: String,
    private val properties: NoticeQuestionProperties,
) : QuestionEmbeddingPort {

    private val log = LoggerFactory.getLogger(GeminiNoticeQuestionAdapter::class.java)
    private val restClient: RestClient = restClientBuilder
        .baseUrl(properties.baseUrl)
        .build()

    override val modelTag: String = "${properties.embeddingModel}@$EMBEDDING_DIMENSIONS"

    override fun embed(text: String): FloatArray {
        val body = mapOf(
            "requests" to listOf(
                mapOf(
                    "model" to "models/${properties.embeddingModel}",
                    "content" to mapOf("parts" to listOf(mapOf("text" to text))),
                    "outputDimensionality" to EMBEDDING_DIMENSIONS,
                ),
            ),
        )
        val values = post("/models/{model}:batchEmbedContents", properties.embeddingModel, body)
            ?.get("embeddings")
            ?.get(0)
            ?.get("values")
        if (values == null || !values.isArray || values.size() != EMBEDDING_DIMENSIONS) {
            log.warn("질문 임베딩 응답이 {} 차원이 아닙니다: {}", EMBEDDING_DIMENSIONS, values?.size())
            throw QuestionException(QuestionErrorCode.AI_UNAVAILABLE)
        }
        return normalize(FloatArray(values.size()) {
            values[it].floatValue()
        })
    }

    private fun post(path: String, model: String, body: Any): JsonNode? {
        if (apiKey.isBlank()) {
            log.error("notice-question.api-key(GEMINI_API_KEY) 가 비어 있어 질문을 처리할 수 없습니다.")
            throw QuestionException(QuestionErrorCode.AI_UNAVAILABLE)
        }
        return try {
            restClient.post()
                .uri(path, model)
                .header(API_KEY_HEADER, apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode::class.java)
        } catch (e: HttpClientErrorException.TooManyRequests) {
            log.warn("Gemini 호출 한도 초과 ({})", model)
            throw QuestionException(QuestionErrorCode.AI_RATE_LIMITED, e)
        } catch (e: HttpClientErrorException) {
            // 401 · 403 · 404 는 키 · 권한 · 모델 이름 문제, 400 은 요청 형식 문제
            log.error("Gemini 가 요청을 거절했습니다 {} ({})", e.statusCode.value(), model)
            throw QuestionException(QuestionErrorCode.AI_UNAVAILABLE, e)
        } catch (e: HttpServerErrorException) {
            log.warn("Gemini 서버 오류 {} ({})", e.statusCode.value(), model)
            throw QuestionException(QuestionErrorCode.AI_UNAVAILABLE, e)
        } catch (e: ResourceAccessException) {
            log.warn("Gemini 에 연결하지 못했습니다 ({}): {}", model, e.javaClass.simpleName)
            throw QuestionException(QuestionErrorCode.AI_UNAVAILABLE, e)
        }
    }

    private fun normalize(vector: FloatArray): FloatArray {
        val norm = sqrt(vector.sumOf {
            (it * it).toDouble()
        }).toFloat()
        if (norm <= 0f) {
            throw QuestionException(QuestionErrorCode.AI_UNAVAILABLE)
        }
        return FloatArray(vector.size) {
            vector[it] / norm
        }
    }

    companion object {
        private const val API_KEY_HEADER = "x-goog-api-key"
    }

}
