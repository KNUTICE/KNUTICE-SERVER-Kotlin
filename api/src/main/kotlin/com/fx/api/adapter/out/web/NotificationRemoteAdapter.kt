package com.fx.api.adapter.out.web

import com.fx.api.application.port.out.NotificationRemotePort
import com.fx.global.annotation.hexagonal.WebOutputAdapter
import com.fx.global.domain.MealType
import com.fx.global.exception.NotificationException
import com.fx.global.exception.errorcode.NotificationErrorCode
import io.github.seob7.Api
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@WebOutputAdapter
class NotificationRemoteAdapter(
    private val crawlerWebClient: WebClient
) : NotificationRemotePort {

    override suspend fun notifyNotice(fcmToken: String, nttId: Long): Boolean {
        return try {
            val response: Api<Boolean> =
                crawlerWebClient.post().uri("/open-api/v1/notification/notice/{nttId}", nttId)
                    .header("fcmToken", fcmToken)
                    .retrieve()
                    .awaitBody()
            response.metaData.isSuccess
        } catch (e: Exception) {
            throw NotificationException(NotificationErrorCode.NOTIFICATION_SEND_FAILED)
        }
    }

    override suspend fun notifyMeal(fcmToken: String, mealType: MealType): Boolean {
        return try {
            val response: Api<Boolean> =
                crawlerWebClient.post().uri("/open-api/v1/notification/meal/{mealType}", mealType)
                    .header("fcmToken", fcmToken)
                    .retrieve()
                    .awaitBody()
            response.metaData.isSuccess
        } catch (e: Exception) {
            throw NotificationException(NotificationErrorCode.NOTIFICATION_SEND_FAILED)
        }
    }

}