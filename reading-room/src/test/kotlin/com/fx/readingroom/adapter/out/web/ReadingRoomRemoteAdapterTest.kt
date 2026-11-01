package com.fx.readingroom.adapter.out.web

import com.fx.readingroom.domain.ReadingRoom
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestClient
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import java.net.InetSocketAddress
import java.time.Duration
import java.time.LocalDateTime

/**
 * 로컬 HTTP 서버를 열람실 사이트 대신 띄워 좌석 응답 파싱을 검증한다.
 * 응답 JSON 은 실제 사이트 응답(제2열람실)에서 가져왔다.
 */
class ReadingRoomRemoteAdapterTest {

    private val server =
        HttpServer.create(InetSocketAddress("localhost", 0), 0).apply {
            createContext(SEATS_ENDPOINT) { exchange ->
                exchange.respondJson(SEATS_RESPONSE)
            }
            start()
        }

    private val adapter =
        ReadingRoomRemoteAdapter(
            restClientBuilder = RestClient.builder(),
            jsonMapper = JsonMapper.builder()
                .addModule(KotlinModule.Builder().build())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build(),
            rootUrl = "http://localhost:${server.address.port}",
            seatsEndpoint = SEATS_ENDPOINT,
            statusEndpoint = STATUS_ENDPOINT,
            connectTimeout = Duration.ofSeconds(1),
            readTimeout = Duration.ofSeconds(1),
        )

    @AfterEach
    fun tearDown() {
        server.stop(0)
    }

    @Test
    fun `반납 기록이 없는 좌석은 returnAt 이 null 이고 나머지 좌석은 그대로 읽는다`() {
        val seats = adapter.getReadingRoomSeats(ReadingRoom.ROOM2, "csrf-token")

        assertThat(seats.map {
            it.seatNumber to it.returnAt
        }).containsExactly(
            301 to LocalDateTime.of(2026, 10, 31, 22, 1, 28, 847_000_000),
            313 to null,
        )
    }

    private fun HttpExchange.respondJson(body: String) {
        val bytes = body.toByteArray()
        responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
        sendResponseHeaders(200, bytes.size.toLong())
        responseBody.use {
            it.write(bytes)
        }
    }

    companion object {
        private const val SEATS_ENDPOINT = "/api/seat/configs"
        private const val STATUS_ENDPOINT = "/api/did/rooms"

        private val SEATS_RESPONSE =
            """
            {
              "result": {
                "CODE": "200",
                "items": [
                  {"room_no": 2, "number": 301, "x_pos": 2, "y_pos": 2, "seat_name": "301", "seat_usage": "1", "seat_type": 1, "use_type": 0, "use_type_nm": "이용가능", "seat_return": 1793451688847, "user_sex": " "},
                  {"room_no": 2, "number": 313, "x_pos": 5, "y_pos": 3, "seat_name": "313", "seat_usage": "1", "seat_type": 1, "use_type": 0, "use_type_nm": "이용가능", "user_sex": " "}
                ]
              }
            }
            """.trimIndent()
    }

}
