package com.fx.crawler.adapter.out.ai

import com.fx.common.domain.notice.EMBEDDING_DIMENSIONS
import com.fx.crawler.config.CrawlerProperties
import com.fx.crawler.domain.embedding.EmbeddingInput
import com.fx.crawler.domain.embedding.EmbeddingRejectedException
import com.fx.crawler.domain.embedding.EmbeddingUnavailableException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest
import org.springframework.test.web.client.response.MockRestResponseCreators.withServiceUnavailable
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.test.web.client.response.MockRestResponseCreators.withTooManyRequests
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Base64

class GeminiEmbeddingAdapterTest {

    private val builder = RestClient.builder()
    private val server = MockRestServiceServer.bindTo(builder).build()
    private val clock = MutableClock(Instant.parse("2026-11-16T03:00:00Z"))
    private val adapter = GeminiEmbeddingAdapter(
        builder,
        "test-key",
        CrawlerProperties(
            embedding = CrawlerProperties.Embedding(
                baseUrl = "https://gemini.test/v1beta",
                requestInterval = Duration.ZERO,
                rateLimitCooldown = Duration.ofMinutes(1),
                maxBatchSize = 2,
            ),
        ),
        clock,
    )

    private val url = "https://gemini.test/v1beta/models/gemini-embedding-2:batchEmbedContents"

    /** 벡터마다 [index] 칸에 3, 다음 칸에 4 를 둔 응답. 정규화하면 0.6 · 0.8 이 된다. */
    private fun response(vararg indexes: Int, dimensions: Int = EMBEDDING_DIMENSIONS): String =
        indexes.joinToString(",", """{"embeddings":[""", "]}") { index ->
            val values = List(dimensions) {
                when (it) {
                    index -> "3.0"
                    index + 1 -> "4.0"
                    else -> "0.0"
                }
            }
            """{"values":[${values.joinToString(",")}]}"""
        }

    private fun expectCall() =
        server.expect(requestTo(url)).andExpect(method(HttpMethod.POST))

    @Test
    fun `텍스트는 묶어 보내고 이미지는 한 장씩 보내며, 입력 순서대로 정규화한 벡터를 돌려준다`() {
        val image = byteArrayOf(1, 2, 3)
        expectCall()
            .andExpect(header("x-goog-api-key", "test-key"))
            .andExpect(jsonPath("$.requests.length()").value(2))
            .andExpect(jsonPath("$.requests[0].model").value("models/gemini-embedding-2"))
            .andExpect(jsonPath("$.requests[0].outputDimensionality").value(EMBEDDING_DIMENSIONS))
            .andExpect(jsonPath("$.requests[0].content.parts[0].text").value("본문 1"))
            .andExpect(jsonPath("$.requests[1].content.parts[0].text").value("본문 2"))
            .andRespond(withSuccess(response(0, 2), MediaType.APPLICATION_JSON))
        expectCall()
            .andExpect(jsonPath("$.requests.length()").value(1))
            .andExpect(jsonPath("$.requests[0].content.parts[0].text").value("본문 3"))
            .andRespond(withSuccess(response(4), MediaType.APPLICATION_JSON))
        expectCall()
            .andExpect(jsonPath("$.requests.length()").value(1))
            .andExpect(jsonPath("$.requests[0].content.parts[0].inlineData.mimeType").value("image/png"))
            .andExpect(jsonPath("$.requests[0].content.parts[0].inlineData.data").value(Base64.getEncoder().encodeToString(image)))
            .andRespond(withSuccess(response(6), MediaType.APPLICATION_JSON))

        val vectors = adapter.embed(
            listOf(
                EmbeddingInput.Text("본문 1"),
                EmbeddingInput.Text("본문 2"),
                EmbeddingInput.Text("본문 3"),
                EmbeddingInput.Image(image, "image/png"),
            )
        )

        server.verify()
        assertThat(vectors).hasSize(4)
        vectors.forEachIndexed { i, vector ->
            assertThat(vector).hasSize(EMBEDDING_DIMENSIONS)
            assertThat(vector[i * 2]).isCloseTo(0.6f, within(1e-6f))
            assertThat(vector[i * 2 + 1]).isCloseTo(0.8f, within(1e-6f))
        }
        assertThat(adapter.modelTag).isEqualTo("gemini-embedding-2@768")
    }

    @Test
    fun `호출 한도에 걸리면 쿨다운 동안 요청하지 않는다`() {
        expectCall().andRespond(withTooManyRequests())

        assertThatThrownBy {
            adapter.embed(listOf(EmbeddingInput.Text("본문")))
        }.isInstanceOf(EmbeddingUnavailableException::class.java)
        // 한도 응답 뒤에는 다시 시도하지도, 다음 공지를 보내지도 않는다
        assertThatThrownBy {
            adapter.embed(listOf(EmbeddingInput.Text("본문")))
        }.isInstanceOf(EmbeddingUnavailableException::class.java)
        assertThatThrownBy {
            adapter.checkAvailable()
        }.isInstanceOf(EmbeddingUnavailableException::class.java)
        server.verify()

        server.reset()
        expectCall().andRespond(withSuccess(response(0), MediaType.APPLICATION_JSON))
        clock.now = clock.now.plus(Duration.ofMinutes(1))

        assertThat(adapter.embed(listOf(EmbeddingInput.Text("본문")))).hasSize(1)
        server.verify()
    }

    @Test
    fun `하루 한도에 걸리면 1시간 동안 요청하지 않는다`() {
        expectCall().andRespond(
            withTooManyRequests().body(
                """{"error":{"code":429,"details":[{"violations":[{"quotaId":"EmbedContentRequestsPerDayPerProjectPerModel-FreeTier"}]}]}}"""
            ).contentType(MediaType.APPLICATION_JSON)
        )

        assertThatThrownBy {
            adapter.embed(listOf(EmbeddingInput.Text("본문")))
        }.isInstanceOf(EmbeddingUnavailableException::class.java)

        // 분당 한도처럼 1분 뒤에 풀리지 않는다
        clock.now = clock.now.plus(Duration.ofMinutes(59))
        assertThatThrownBy {
            adapter.checkAvailable()
        }.isInstanceOf(EmbeddingUnavailableException::class.java)

        clock.now = clock.now.plus(Duration.ofMinutes(1))
        adapter.checkAvailable()
        server.verify()
    }

    @Test
    fun `서버 오류는 다시 시도한다`() {
        expectCall().andRespond(withServiceUnavailable())
        expectCall().andRespond(withSuccess(response(0), MediaType.APPLICATION_JSON))

        assertThat(adapter.embed(listOf(EmbeddingInput.Text("본문")))).hasSize(1)
        server.verify()
    }

    @Test
    fun `다시 시도해도 서버 오류면 그 공지는 실패시키고 장애로 보고 요청을 멈춘다`() {
        repeat(3) {
            expectCall().andRespond(withServiceUnavailable())
        }

        assertThatThrownBy {
            adapter.embed(listOf(EmbeddingInput.Text("본문")))
        }.isInstanceOf(HttpServerErrorException::class.java)
        assertThatThrownBy {
            adapter.checkAvailable()
        }.isInstanceOf(EmbeddingUnavailableException::class.java)
        server.verify()

        clock.now = clock.now.plus(Duration.ofMinutes(10))
        adapter.checkAvailable()
    }

    @Test
    fun `키 · 권한 오류는 다시 시도하지 않고 그 공지는 실패시킨 뒤 요청을 멈춘다`() {
        expectCall().andRespond(withStatus(HttpStatus.FORBIDDEN))

        assertThatThrownBy {
            adapter.embed(listOf(EmbeddingInput.Text("본문")))
        }.isInstanceOf(HttpClientErrorException.Forbidden::class.java)
        assertThatThrownBy {
            adapter.checkAvailable()
        }.isInstanceOf(EmbeddingUnavailableException::class.java)
        server.verify()
    }

    @Test
    fun `모델이 받지 않는 입력은 다시 시도하지 않고 거부로 알리며 요청을 멈추지 않는다`() {
        expectCall().andRespond(withBadRequest())

        assertThatThrownBy {
            adapter.embed(listOf(EmbeddingInput.Image(byteArrayOf(1), "image/png")))
        }.isInstanceOf(EmbeddingRejectedException::class.java)
        adapter.checkAvailable()
        server.verify()
    }

    @Test
    fun `차원이 다른 벡터가 오면 실패시킨다`() {
        expectCall().andRespond(withSuccess(response(0, dimensions = 3), MediaType.APPLICATION_JSON))

        assertThatThrownBy {
            adapter.embed(listOf(EmbeddingInput.Text("본문")))
        }.isInstanceOf(IllegalStateException::class.java)
    }

    private class MutableClock(var now: Instant) : Clock() {
        override fun instant(): Instant =
            now
        override fun getZone(): ZoneId =
            ZoneOffset.UTC
        override fun withZone(zone: ZoneId?): Clock =
            this
    }

}
