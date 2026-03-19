package com.fx.readingroom.adapter.out.web

import com.fx.global.annotation.hexagonal.WebOutputAdapter
import com.fx.readingroom.adapter.out.web.dto.ReadingRoomSeatRemoteResponse
import com.fx.readingroom.adapter.out.web.dto.ReadingRoomStatusRemoteResponse
import com.fx.readingroom.application.port.out.ReadingRoomRemotePort
import com.fx.readingroom.domain.ReadingRoom
import com.fx.readingroom.domain.ReadingRoomSeat
import com.fx.readingroom.domain.ReadingRoomStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.web.reactive.function.client.bodyToMono
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.time.Instant
import java.time.ZoneId

@WebOutputAdapter
class ReadingRoomRemoteAdapter(
    private val webClient: WebClient,
    @Value("\${reading-room.root-url}") private val rootUrl: String,
    @Value("\${reading-room.endpoints.seats}") private val seatsEndpoint: String,
    @Value("\${reading-room.endpoints.status}") private val statusEndpoint: String,
) : ReadingRoomRemotePort {

    private val log = LoggerFactory.getLogger(ReadingRoomRemoteAdapter::class.java)

    /**
     * 초기 접속을 통해 CSRF 토큰을 추출하고 세션을 수립합니다.
     */
    override suspend fun getCsrfToken(): String {
        val html = webClient.get()
            .uri(rootUrl)
            .retrieve()
            .bodyToMono<String>()
            .awaitSingle()

        val document = withContext(Dispatchers.Default) { Jsoup.parse(html) }
        return document.getElementById("token")?.attr("value")
            ?: throw IllegalStateException("CSRF 토큰을 찾을 수 없습니다.")
    }

    /**
     * 열람실별 전체 현황(요약 정보) 조회
     */
    override suspend fun getReadingRoomStatus(): List<ReadingRoomStatus> {
        val fetchStatus = suspend {
            webClient.get()
                .uri { it.path("$rootUrl$statusEndpoint").queryParam("caller", "nicom").build() }
                .retrieve()
                .bodyToMono<ReadingRoomStatusRemoteResponse>()
                .awaitSingle()
        }

        val response = try {
            fetchStatus()
        } catch (e: Exception) {
            // dslee (2026.02.10) : try 구문에서 html 응답이 오는 경우, 재시도 처리
            log.error("열람실 현황 조회 실패, 재시도 수행: ${e.message}")
            fetchStatus() // 재시도 로직 유지
        }

         return response.result.items.map { item ->
            ReadingRoomStatus(
                roomId = ReadingRoom.from(item.room_no),
                roomName = item.name,
                totalSeat = item.total_count,
                availableSeat = item.remain_count,
                occupiedSeat = item.usage_count,
                rowCount = item.rows,
                columnCount = item.cols
            )
        }
    }

    /**
     * 특정 열람실의 상세 좌석 정보 조회
     */
    override suspend fun getReadingRoomSeats(readingRoom: ReadingRoom, csrfToken: String): List<ReadingRoomSeat> {
        val response = webClient.post()
            .uri("$rootUrl$seatsEndpoint")
            .header("x-csrf-token", csrfToken)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                BodyInserters.fromFormData("caller", "nicom")
                    .with("room_no", readingRoom.roomId.toString())
            )
            .retrieve()
            .bodyToMono<ReadingRoomSeatRemoteResponse>()
            .awaitSingle()

        return response.result.items.map { item ->
            ReadingRoomSeat(
                roomId = ReadingRoom.from(item.room_no),
                seatNumber = item.number,
                row = item.y_pos,
                column = item.x_pos,
                isAvailable = item.use_type == 0,
                userMaskedName = item.user_name,
                returnAt = Instant.ofEpochMilli(item.seat_return)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toLocalDateTime()
            )
        }
    }

}