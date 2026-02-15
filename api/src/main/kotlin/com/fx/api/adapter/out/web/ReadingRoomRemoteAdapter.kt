package com.fx.api.adapter.out.web

import com.fx.api.adapter.out.web.dto.ReadingRoomSeatRemoteResponse
import com.fx.api.adapter.out.web.dto.ReadingRoomStatusRemoteResponse
import com.fx.api.application.port.out.ReadingRoomRemotePort
import com.fx.api.domain.ReadingRoomSeat
import com.fx.api.domain.ReadingRoomStatus
import com.fx.global.annotation.hexagonal.WebOutputAdapter
import com.fx.global.domain.readingroom.ReadingRoom
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import java.time.Instant
import java.time.ZoneId

@WebOutputAdapter
class ReadingRoomRemoteAdapter(
    private val httpClient: HttpClient, // 위에서 설정한 HttpCookies가 설치된 빈
    @Value("\${reading-room.root-url}") private val rootUrl: String,
    @Value("\${reading-room.endpoints.seats}") private val seatsEndpoint: String,
    @Value("\${reading-room.endpoints.status}") private val statusEndpoint: String,
    ) : ReadingRoomRemotePort {

    private val log = LoggerFactory.getLogger(ReadingRoomRemoteAdapter::class.java)

    /**
     * 초기 접속을 통해 CSRF 토큰을 추출하고 세션을 수립합니다.
     * 발급된 세션 쿠키는 Ktor의 [HttpCookies] 플러그인에 의해 자동으로 저장 및 관리됩니다.
     */
    override suspend fun getCsrfToken(): String = coroutineScope {
        val response = httpClient.get(rootUrl)
        val html: String = response.body()
        val document = withContext(Dispatchers.Default) { Jsoup.parse(html) }

        document.getElementById("token")?.attr("value")
            ?: throw IllegalStateException("CSRF 토큰을 찾을 수 없습니다.")
    }

    /**
     * 열람실별 전체 현황(요약 정보) 조회
     */
    override suspend fun getReadingRoomStatus(): List<ReadingRoomStatus> = coroutineScope {
        val response: ReadingRoomStatusRemoteResponse = httpClient.get("$rootUrl$statusEndpoint") {
            parameter("caller", "nicom")
        }.body()

        response.result.items.map { item ->
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
    override suspend fun getReadingRoomSeats(readingRoom: ReadingRoom, csrfToken: String): List<ReadingRoomSeat> = coroutineScope {
        val response: ReadingRoomSeatRemoteResponse = httpClient.post("$rootUrl$seatsEndpoint") {
            header("x-csrf-token", csrfToken)
            setBody(FormDataContent(Parameters.build {
                append("caller", "nicom")
                append("room_no", readingRoom.roomId.toString())
            }))
        }.body()

        response.result.items.map { item ->
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