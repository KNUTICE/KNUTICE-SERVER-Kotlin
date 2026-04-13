package com.fx.readingroom.adapter.out.persistence.repository

import com.fx.readingroom.adapter.out.persistence.document.SeatAlertDocument
import com.fx.readingroom.domain.SeatAlert.SeatAlertStatus
import org.springframework.data.mongodb.repository.MongoRepository

interface SeatAlertMongoRepository : MongoRepository<SeatAlertDocument, String> {

    fun findByFcmTokenAndStatusOrderByCreatedAtDesc(
        fcmToken: String,
        seatAlertStatus: SeatAlertStatus
    ): List<SeatAlertDocument>

    fun deleteByFcmTokenAndId(fcmToken: String, id: String): Int

    fun findByStatus(seatAlertStatus: SeatAlertStatus): List<SeatAlertDocument>

}