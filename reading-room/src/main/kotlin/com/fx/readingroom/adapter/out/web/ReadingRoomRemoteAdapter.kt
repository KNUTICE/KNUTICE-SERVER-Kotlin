package com.fx.readingroom.adapter.out.web

import com.fx.common.annotation.hexagonal.WebOutputAdapter
import com.fx.common.exception.ConnectionException
import com.fx.common.exception.errorcode.ConnectionErrorCode
import com.fx.readingroom.adapter.out.web.dto.ReadingRoomSeatRemoteResponse
import com.fx.readingroom.adapter.out.web.dto.ReadingRoomStatusRemoteResponse
import com.fx.readingroom.application.port.out.ReadingRoomRemotePort
import com.fx.readingroom.domain.ReadingRoom
import com.fx.readingroom.domain.ReadingRoomSeat
import com.fx.readingroom.domain.ReadingRoomStatus
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper
import java.net.CookieManager
import java.net.http.HttpClient
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/**
 * 열람실 좌석 사이트 호출.
 *
 * 좌석 조회는 CSRF 토큰과 같은 세션 쿠키가 필요하므로, JDK HttpClient 에 [CookieManager] 를 달아
 * 이 어댑터의 모든 요청이 쿠키를 공유하게 한다.
 * 사이트가 JSON 대신 HTML 을 돌려주는 경우가 있어, 응답은 문자열로 받아 직접 파싱하고 한 번 다시 시도한다.
 * 연결 실패 · 오류 응답은 [ConnectionException] (503) 으로 바꾼다.
 */
@WebOutputAdapter
class ReadingRoomRemoteAdapter(
    restClientBuilder: RestClient.Builder,
    private val jsonMapper: JsonMapper,
    @param:Value("\${reading-room.root-url}") private val rootUrl: String,
    @param:Value("\${reading-room.endpoints.seats}") private val seatsEndpoint: String,
    @param:Value("\${reading-room.endpoints.status}") private val statusEndpoint: String,
    @Value("\${reading-room.timeout.connect}") connectTimeout: Duration,
    @Value("\${reading-room.timeout.read}") readTimeout: Duration,
) : ReadingRoomRemotePort {

    private val log = LoggerFactory.getLogger(ReadingRoomRemoteAdapter::class.java)

    private val restClient: RestClient = restClientBuilder.clone()
        .requestFactory(
            JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                    .cookieHandler(CookieManager())
                    .connectTimeout(connectTimeout)
                    .build()
            ).apply { setReadTimeout(readTimeout) }
        )
        .build()

    override fun getCsrfToken(): String {
        val html = call {
            restClient.get()
                .uri(rootUrl)
                .retrieve()
                .body(String::class.java)
        }

        return Jsoup.parse(html).getElementById("token")?.attr("value")
            ?: throw IllegalStateException("CSRF 토큰을 찾을 수 없습니다.")
    }

    override fun getReadingRoomStatus(): List<ReadingRoomStatus> {
        val response = withOneRetry("열람실 현황 조회") {
            val body = call {
                restClient.get()
                    .uri("$rootUrl$statusEndpoint?caller={caller}", CALLER)
                    .retrieve()
                    .body(String::class.java)
            }
            jsonMapper.readValue(body, ReadingRoomStatusRemoteResponse::class.java)
        }

        return response.result.items.map { item ->
            ReadingRoomStatus(
                roomId = ReadingRoom.from(item.roomNo),
                roomName = item.name,
                totalSeat = item.totalCount,
                availableSeat = item.remainCount,
                occupiedSeat = item.usageCount,
                rowCount = item.rows,
                columnCount = item.cols,
            )
        }
    }

    override fun getReadingRoomSeats(readingRoom: ReadingRoom, csrfToken: String): List<ReadingRoomSeat> {
        val form = LinkedMultiValueMap<String, String>().apply {
            add("caller", CALLER)
            add("room_no", readingRoom.roomId.toString())
        }

        val body = call {
            restClient.post()
                .uri("$rootUrl$seatsEndpoint")
                .header("x-csrf-token", csrfToken)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(String::class.java)
        }
        val response = jsonMapper.readValue(body, ReadingRoomSeatRemoteResponse::class.java)

        return response.result.items.map { item ->
            ReadingRoomSeat(
                roomId = ReadingRoom.from(item.roomNo),
                seatNumber = item.number,
                row = item.yPos,
                column = item.xPos,
                isAvailable = item.useType == 0,
                userMaskedName = item.userName,
                returnAt = Instant.ofEpochMilli(item.seatReturn).atZone(SEOUL).toLocalDateTime(),
            )
        }
    }

    private fun call(request: () -> String?): String =
        try {
            request().orEmpty()
        } catch (e: RestClientException) {
            log.warn("열람실 사이트 호출 실패 - {}", e.message)
            throw ConnectionException(ConnectionErrorCode.REMOTE_SERVER_UNAVAILABLE)
        }

    /** 사이트가 JSON 대신 HTML 을 돌려주면 파싱이 실패한다. 이 경우 한 번만 다시 요청한다. */
    private fun <T> withOneRetry(action: String, request: () -> T): T =
        try {
            request()
        } catch (e: JacksonException) {
            log.warn("{} 응답 파싱 실패 (HTML 응답 추정). 한 번 다시 시도합니다. - {}", action, e.message)
            request()
        }

    companion object {
        private const val CALLER = "nicom"
        private val SEOUL: ZoneId = ZoneId.of("Asia/Seoul")
    }

}
