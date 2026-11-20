package com.fx.api.adapter.out.ai

import com.fx.api.config.question.NoticeQuestionProperties
import com.fx.api.exception.QuestionException
import com.fx.api.exception.errorcode.QuestionErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.test.web.client.response.MockRestResponseCreators.withTooManyRequests
import org.springframework.http.HttpStatus
import org.springframework.web.client.RestClient
import kotlin.math.sqrt

class GeminiNoticeQuestionAdapterTest {

    private val builder = RestClient.builder()
    private val server = MockRestServiceServer.bindTo(builder).build()
    private val properties = NoticeQuestionProperties(baseUrl = "https://gemini.test/v1beta")
    private val adapter = GeminiNoticeQuestionAdapter(builder, "test-key", properties)

    @Test
    fun `질문을 공지와 같은 요청(768차원, taskType 없음)으로 임베딩하고 길이 1 로 정규화한다`() {
        val values = (1..768).joinToString(",") {
            if (it <= 2) "3" else "0"
        }
        server.expect(requestTo("https://gemini.test/v1beta/models/gemini-embedding-2:batchEmbedContents"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("x-goog-api-key", "test-key"))
            .andExpect(jsonPath("$.requests[0].model").value("models/gemini-embedding-2"))
            .andExpect(jsonPath("$.requests[0].outputDimensionality").value(768))
            .andExpect(jsonPath("$.requests[0].content.parts[0].text").value("수강신청 언제야?"))
            .andExpect(jsonPath("$.requests[0].taskType").doesNotExist())
            .andRespond(withSuccess("""{"embeddings":[{"values":[$values]}]}""", MediaType.APPLICATION_JSON))

        val vector = adapter.embed("수강신청 언제야?")

        assertThat(vector).hasSize(768)
        assertThat(vector[0].toDouble()).isCloseTo(1 / sqrt(2.0), within(1e-6))
        assertThat(adapter.modelTag).isEqualTo("gemini-embedding-2@768")
        server.verify()
    }

    @Test
    fun `답변 글을 이어 붙이고 생각(thought) 부분과 토큰 수를 구분한다`() {
        server.expect(requestTo("https://gemini.test/v1beta/models/gemini-3.5-flash-lite:generateContent"))
            .andExpect(jsonPath("$.systemInstruction.parts[0].text").value("system"))
            .andExpect(jsonPath("$.contents[0].parts[0].text").value("user"))
            .andRespond(
                withSuccess(
                    """
                    {"candidates":[{"content":{"parts":[{"text":"생각 중","thought":true},{"text":" 11월 17일부터예요. "},{"text":"[1]"}]}}],
                     "usageMetadata":{"promptTokenCount":3120,"candidatesTokenCount":96}}
                    """.trimIndent(),
                    MediaType.APPLICATION_JSON,
                )
            )

        val answer = adapter.generate("system", "user")

        assertThat(answer.text).isEqualTo("11월 17일부터예요. [1]")
        assertThat(answer.inputTokens).isEqualTo(3120)
        assertThat(answer.outputTokens).isEqualTo(96)
    }

    @Test
    fun `429 는 호출 한도, 그 밖의 거절은 AI 장애로 알린다`() {
        server.expect(requestTo("https://gemini.test/v1beta/models/gemini-embedding-2:batchEmbedContents"))
            .andRespond(withTooManyRequests())
        server.expect(requestTo("https://gemini.test/v1beta/models/gemini-embedding-2:batchEmbedContents"))
            .andRespond(withStatus(HttpStatus.FORBIDDEN))

        assertThatThrownBy {
            adapter.embed("질문")
        }.isInstanceOfSatisfying(QuestionException::class.java) {
            assertThat(it.baseErrorCode).isEqualTo(QuestionErrorCode.AI_RATE_LIMITED)
        }
        assertThatThrownBy {
            adapter.embed("질문")
        }.isInstanceOfSatisfying(QuestionException::class.java) {
            assertThat(it.baseErrorCode).isEqualTo(QuestionErrorCode.AI_UNAVAILABLE)
        }
    }

    @Test
    fun `키가 비어 있으면 요청하지 않고 AI 장애로 알린다`() {
        val noKey = GeminiNoticeQuestionAdapter(RestClient.builder(), "", properties)

        assertThatThrownBy {
            noKey.embed("질문")
        }.isInstanceOfSatisfying(QuestionException::class.java) {
            assertThat(it.baseErrorCode).isEqualTo(QuestionErrorCode.AI_UNAVAILABLE)
        }
    }

}
