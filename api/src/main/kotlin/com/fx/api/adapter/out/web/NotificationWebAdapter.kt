package com.fx.api.adapter.out.web

import com.fx.api.adapter.out.web.client.NotificationClient
import com.fx.api.application.port.out.NotificationWebPort
import com.fx.common.annotation.hexagonal.WebOutputAdapter
import com.fx.common.domain.MealType
import com.fx.common.exception.NotificationException
import com.fx.common.exception.errorcode.NotificationErrorCode

@WebOutputAdapter
class NotificationWebAdapter(
    private val notificationClient: NotificationClient
) : NotificationWebPort {

    override fun notifyNotice(fcmToken: String, nttId: Long): Boolean {
        return try {
            val response = notificationClient.notifyNotice(fcmToken, nttId)
            response.body?.metaData?.isSuccess ?: false
        } catch (e: Exception) {
            throw NotificationException(NotificationErrorCode.NOTIFICATION_SEND_FAILED)
        }
    }

    override fun notifyMeal(fcmToken: String, mealType: MealType): Boolean {
        return try {
            val response = notificationClient.notifyMeal(fcmToken, mealType)
            response.body?.metaData?.isSuccess ?: false
        } catch (e: Exception) {
            throw NotificationException(NotificationErrorCode.NOTIFICATION_SEND_FAILED)
        }
    }
}