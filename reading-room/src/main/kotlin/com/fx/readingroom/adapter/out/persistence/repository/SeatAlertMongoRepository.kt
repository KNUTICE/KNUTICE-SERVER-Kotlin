package com.fx.readingroom.adapter.out.persistence.repository

import com.fx.readingroom.adapter.out.persistence.document.SeatAlertDocument
import com.fx.readingroom.domain.SeatAlert.SeatAlertStatus
import kotlinx.coroutines.flow.Flow
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface SeatAlertMongoRepository : CoroutineCrudRepository<SeatAlertDocument, String> {

    fun findByFcmTokenAndStatusOrderByCreatedAtDesc(
        fcmToken: String,
        seatAlertStatus: SeatAlertStatus
    ): Flow<SeatAlertDocument>

    suspend fun deleteByFcmTokenAndId(fcmToken: String, id: String): Long // Int 보다는 Long 권장

    fun findByStatus(seatAlertStatus: SeatAlertStatus): Flow<SeatAlertDocument>

}