package com.fx.crawler.legacy.readingroom

import com.fx.crawler.legacy.readingroom.SeatAlert.SeatAlertStatus
import org.springframework.data.mongodb.repository.MongoRepository

interface SeatAlertMongoRepository : MongoRepository<SeatAlertDocument, String> {

    fun findByFcmTokenAndStatusOrderByCreatedAtDesc(
        fcmToken: String,
        seatAlertStatus: SeatAlertStatus
    ): List<SeatAlertDocument>

    fun deleteByFcmTokenAndId(fcmToken: String, id: String): Int

    fun findByStatus(seatAlertStatus: SeatAlertStatus): List<SeatAlertDocument>

}