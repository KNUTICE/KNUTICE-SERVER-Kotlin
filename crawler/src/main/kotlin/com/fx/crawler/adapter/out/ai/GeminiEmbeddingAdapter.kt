package com.fx.crawler.adapter.out.ai

import com.fx.common.domain.notice.EMBEDDING_DIMENSIONS
import com.fx.crawler.application.port.out.NoticeEmbeddingPort
import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.embedding.EmbeddingInput
import com.fx.crawler.domain.embedding.EmbeddingRejectedException
import com.fx.crawler.domain.embedding.EmbeddingUnavailableException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import tools.jackson.databind.JsonNode
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Base64
import kotlin.math.sqrt

/**
 * Gemini 기본 API(`batchEmbedContents`) 임베딩. 텍스트와 이미지를 같은 벡터 공간으로 임베딩한다.
 * 요약이 쓰는 Spring AI(OpenAI 호환) 경로는 임베딩이 텍스트 전용이라 RestClient 로 직접 부른다.
 *
 * - 텍스트는 `crawler.embedding.max-batch-size` 개씩 묶어 보내고, 이미지는 요청 크기를 줄이려고 한 장씩 보낸다.
 * - 무료 한도는 묶음 요청도 입력마다 한 번으로 세므로, 요청마다 입력 수만큼 `request-interval` 을 기다린다 (재시도 포함).
 * - 호출 한도 응답(429)을 받으면 `rate-limit-cooldown`(하루 한도면 `daily-limit-cooldown`) 동안 요청하지 않고
 *   [EmbeddingUnavailableException] 을 던진다. 멈춘 동안에도 같은 예외를 던진다 ([checkAvailable]).
 * - 입력이 잘못된 오류(400)는 [EmbeddingRejectedException] 으로 알린다. 다시 보내도 같으므로 재시도하지 않는다.
 * - 공지와 상관없는 오류(401 · 403 · 404, 재시도해도 남는 서버 · 네트워크 오류)는 `error-cooldown` 동안 요청을 멈춘 뒤 그대로 던진다.
 *   그 공지만 실패로 세고, 멈춘 동안의 공지는 미뤄진다.
 * - 서버 · 네트워크 오류는 1초 · 2초 뒤 다시 시도한다.
 * - 768차원처럼 잘라 받은 벡터는 길이가 1 이 아니므로 정규화해서 돌려준다 (코사인 거리 비교용).
 */
@Component
class GeminiEmbeddingAdapter(
    restClientBuilder: RestClient.Builder,
    @Value("\${crawler.embedding.api-key}") apiKey: String,
    properties: CrawlerProperties,
    private val clock: Clock,
) : NoticeEmbeddingPort {

    private val log = LoggerFactory.getLogger(GeminiEmbeddingAdapter::class.java)
    private val embedding = properties.embedding
    private val throttle = RequestThrottle(embedding.requestInterval, clock)
    private val restClient: RestClient = restClientBuilder
        .baseUrl(embedding.baseUrl)
        .defaultHeader(API_KEY_HEADER, apiKey)
        .build()

    @Volatile
    private var pausedUntil: Instant = Instant.MIN

    override val modelTag: String = "${embedding.model}@$EMBEDDING_DIMENSIONS"

    override fun checkAvailable() =
        checkNotPaused()

    override fun embed(inputs: List<EmbeddingInput>): List<FloatArray> =
        batches(inputs).flatMap {
            embedBatch(it)
        }

    /** 텍스트는 묶고 이미지는 한 장씩 나눈다. 입력 순서는 그대로 둔다. */
    private fun batches(inputs: List<EmbeddingInput>): List<List<EmbeddingInput>> {
        val batches = mutableListOf<List<EmbeddingInput>>()
        val texts = mutableListOf<EmbeddingInput>()
        fun flushTexts() {
            if (texts.isNotEmpty()) {
                batches += texts.toList()
                texts.clear()
            }
        }
        inputs.forEach { input ->
            when (input) {
                is EmbeddingInput.Text -> {
                    texts += input
                    if (texts.size >= embedding.maxBatchSize) {
                        flushTexts()
                    }
                }
                is EmbeddingInput.Image -> {
                    flushTexts()
                    batches += listOf(input)
                }
            }
        }
        flushTexts()
        return batches
    }

    private fun embedBatch(batch: List<EmbeddingInput>): List<FloatArray> {
        checkNotPaused()
        val body = mapOf(
            "requests" to batch.map {
                request(it)
            },
        )
        var backoff = INITIAL_BACKOFF

        repeat(MAX_ATTEMPTS) { attempt ->
            repeat(batch.size) {
                throttle.acquire()
            }
            try {
                val response = restClient.post()
                    .uri("/models/{model}:batchEmbedContents", embedding.model)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode::class.java)
                return parse(response, batch.size)
            } catch (e: HttpClientErrorException.TooManyRequests) {
                // 하루 한도는 응답의 quotaId 에 PerDay 가 들어 있다. 다음 실행마다 학교 사이트를 헛되이 읽지 않도록 길게 멈춘다
                val daily = e.responseBodyAsString.contains(DAILY_QUOTA_MARK)
                val reason = if (daily) "임베딩 하루 호출 한도 초과" else "임베딩 호출 한도 초과"
                pause(if (daily) embedding.dailyLimitCooldown else embedding.rateLimitCooldown, reason)
                throw EmbeddingUnavailableException("$reason: ${e.statusText}", e)
            } catch (e: HttpClientErrorException.BadRequest) {
                throw EmbeddingRejectedException("임베딩 모델이 입력을 받지 않음: ${e.statusText}", e)
            } catch (e: HttpClientErrorException) {
                // 401 · 403 · 404 는 키 · 권한 · 모델 이름 문제라 어느 공지든 같다. 이 공지만 실패로 세고 멈춘다
                pause(embedding.errorCooldown, "임베딩 설정 오류 ${e.statusCode.value()}")
                throw e
            } catch (e: Exception) {
                if (!isRetryable(e)) {
                    throw e
                }
                if (attempt == MAX_ATTEMPTS - 1) {
                    // 다시 보내도 서버 · 네트워크 오류면 Gemini 장애로 본다
                    pause(embedding.errorCooldown, "임베딩 서버 · 네트워크 오류")
                    throw e
                }
                log.warn("임베딩 일시 오류, {}ms 후 다시 시도 ({}/{}) - {}", backoff.toMillis(), attempt + 1, MAX_ATTEMPTS, e.message)
                Thread.sleep(backoff)
                backoff = backoff.multipliedBy(2)
            }
        }
        error("도달할 수 없는 코드")
    }

    private fun request(input: EmbeddingInput): Map<String, Any> =
        mapOf(
            "model" to "models/${embedding.model}",
            "content" to mapOf("parts" to listOf(part(input))),
            "outputDimensionality" to EMBEDDING_DIMENSIONS,
        )

    private fun part(input: EmbeddingInput): Map<String, Any> =
        when (input) {
            is EmbeddingInput.Text -> mapOf("text" to input.text)
            is EmbeddingInput.Image -> mapOf(
                "inlineData" to mapOf(
                    "mimeType" to input.mimeType,
                    "data" to Base64.getEncoder().encodeToString(input.bytes),
                ),
            )
        }

    private fun parse(response: JsonNode?, expected: Int): List<FloatArray> {
        val embeddings = response?.get("embeddings")?.takeIf {
            it.isArray && it.size() == expected
        }
            ?: throw IllegalStateException("임베딩 응답 개수가 요청($expected 개)과 다릅니다.")

        return (0 until embeddings.size()).map { index ->
            val values = embeddings[index]?.get("values")
            check(values != null && values.size() == EMBEDDING_DIMENSIONS) {
                "임베딩 차원이 ${values?.size()} 입니다. ($EMBEDDING_DIMENSIONS 차원이어야 함)"
            }
            normalize(FloatArray(values.size()) {
                values[it].floatValue()
            })
        }
    }

    private fun normalize(vector: FloatArray): FloatArray {
        val norm = sqrt(vector.sumOf { (it * it).toDouble() }).toFloat()
        check(norm > 0f) {
            "길이가 0 인 벡터를 받았습니다."
        }
        return FloatArray(vector.size) {
            vector[it] / norm
        }
    }

    private fun checkNotPaused() {
        val until = pausedUntil
        if (clock.instant().isBefore(until)) {
            throw EmbeddingUnavailableException("임베딩 호출 한도 · 장애로 $until 까지 요청하지 않습니다.")
        }
    }

    private fun pause(cooldown: Duration, reason: String) {
        pausedUntil = clock.instant().plus(cooldown)
        log.warn("{} - {} 까지 임베딩 요청을 멈춤", reason, pausedUntil)
    }

    /** 서버 오류(5xx)와 네트워크 오류만 다시 시도한다. */
    private fun isRetryable(e: Exception): Boolean =
        e is HttpServerErrorException || e is ResourceAccessException

    companion object {
        private const val API_KEY_HEADER = "x-goog-api-key"
        /** 하루 한도 429 응답의 quotaId (예: `EmbedContentRequestsPerDayPerProjectPerModel-FreeTier`) 에 들어 있는 글자 */
        private const val DAILY_QUOTA_MARK = "PerDay"
        private const val MAX_ATTEMPTS = 3
        private val INITIAL_BACKOFF: Duration = Duration.ofSeconds(1)
    }

}
