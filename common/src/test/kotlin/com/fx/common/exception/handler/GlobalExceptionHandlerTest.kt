package com.fx.common.exception.handler

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule

/** 요청 값 오류가 본문 없는 403 이 아니라 공통 400 응답으로 나가는지 확인한다. */
class GlobalExceptionHandlerTest {

    private val mockMvc: MockMvc =
        MockMvcBuilders.standaloneSetup(TestController())
            .setControllerAdvice(GlobalExceptionHandler())
            .setMessageConverters(JacksonJsonHttpMessageConverter(JsonMapper.builder().addModule(KotlinModule.Builder().build()).build()))
            .build()

    @Test
    fun `요청 본문이 올바른 JSON 이 아니면 400 이다`() {
        postBody("""{"name": "a""")
            .andExpectBadRequest("요청 본문이 없거나 올바른 JSON 형식이 아닙니다.")
    }

    @Test
    fun `필수 필드가 없으면 그 필드를 알려 준다`() {
        postBody("""{"count": 1, "parameters": {}}""")
            .andExpectBadRequest("[name](은)는 값이 없거나 형식이 올바르지 않습니다.")
    }

    @Test
    fun `필드 타입이 다르면 그 필드의 경로를 알려 준다`() {
        postBody("""{"name": "a", "count": "많이", "parameters": {}}""")
            .andExpectBadRequest("[count](은)는 값이 없거나 형식이 올바르지 않습니다.")
        postBody("""{"name": "a", "count": 1, "parameters": {"topicType": {"nested": 1}}}""")
            .andExpectBadRequest("[parameters.topicType](은)는 값이 없거나 형식이 올바르지 않습니다.")
    }

    @Test
    fun `경로 변수 타입이 다르면 400 이다`() {
        mockMvc.perform(get("/test/items/abc"))
            .andExpectBadRequest("[id](은)는 형식이 올바르지 않습니다. 입력된 값: [abc]")
    }

    @Test
    fun `없는 enum 값이면 고를 수 있는 값을 알려 준다`() {
        mockMvc.perform(get("/test/items").param("status", "DONE"))
            .andExpectBadRequest("[status](은)는 READY, DONE_OK 중 하나여야 합니다. 입력된 값: [DONE]")
    }

    @Test
    fun `필수 쿼리 파라미터가 없으면 400 이다`() {
        mockMvc.perform(get("/test/required"))
            .andExpectBadRequest("[name](은)는 필수입니다.")
    }

    private fun postBody(json: String): ResultActions =
        mockMvc.perform(post("/test/body").contentType(MediaType.APPLICATION_JSON).content(json))

    private fun ResultActions.andExpectBadRequest(message: String): ResultActions =
        andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.metaData.success").value(false))
            .andExpect(jsonPath("$.metaData.code").value(400))
            .andExpect(jsonPath("$.metaData.message").value(message))

    enum class TestStatus {
        READY,
        DONE_OK,
    }

    data class TestBody(
        val name: String,
        val count: Int,
        val parameters: Map<String, String>,
    )

    @RestController
    class TestController {

        @PostMapping("/test/body")
        fun body(@RequestBody body: TestBody): String =
            body.name

        @GetMapping("/test/items/{id}")
        fun item(@PathVariable id: Long): Long =
            id

        @GetMapping("/test/items")
        fun items(@RequestParam(required = false) status: TestStatus?): String =
            status.toString()

        @GetMapping("/test/required")
        fun required(@RequestParam name: String): String =
            name

    }

}
