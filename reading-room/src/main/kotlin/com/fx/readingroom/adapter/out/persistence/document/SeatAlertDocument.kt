package com.fx.readingroom.adapter.out.persistence.document

import com.fx.global.adapter.out.persistence.document.base.MongoBaseDocument
import com.fx.readingroom.domain.ReadingRoom
import com.fx.readingroom.domain.SeatAlert
import com.fx.readingroom.domain.SeatAlert.SeatAlertStatus
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "seat_alert")
@CompoundIndexes(
    CompoundIndex(
        name = "unique_active_alert",
        def = "{'fcmToken': 1, 'readingRoom': 1, 'seatNumber': 1, 'status': 1}",
        unique = true
    )
)
class SeatAlertDocument(

    @Id
    var id: String? = null,

    var fcmToken: String,

    var readingRoom: ReadingRoom,

    var seatNumber: Int,

    var status: SeatAlertStatus,

    var notifiedAt: LocalDateTime? = null,

    @Indexed(expireAfter = "0s") // 시간은 SeatAlert 도메인에서 결정함
    var expiresAt: LocalDateTime

) : MongoBaseDocument() {

    fun toDomain(): SeatAlert =
        SeatAlert(
            id = id,
            fcmToken = fcmToken,
            readingRoom = readingRoom,
            seatNumber = seatNumber,
            status = status,
            notifiedAt = notifiedAt,
            expiresAt = expiresAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    companion object {
        fun from(seatAlert: SeatAlert): SeatAlertDocument =
            SeatAlertDocument(
                id = seatAlert.id,
                fcmToken = seatAlert.fcmToken,
                readingRoom = seatAlert.readingRoom,
                seatNumber = seatAlert.seatNumber,
                status = seatAlert.status,
                notifiedAt = seatAlert.notifiedAt,
                expiresAt = seatAlert.expiresAt
            ).apply {
                this.createdAt = seatAlert.createdAt
                this.updatedAt = seatAlert.updatedAt
            }
    }

}