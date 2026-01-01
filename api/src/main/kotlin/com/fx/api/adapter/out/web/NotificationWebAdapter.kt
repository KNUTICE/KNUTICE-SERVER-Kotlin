package com.fx.api.adapter.out.web

import com.fx.api.adapter.out.web.client.NotificationFeignClient
import com.fx.api.application.port.out.NotificationWebPort
import com.fx.global.annotation.hexagonal.WebOutputAdapter
import com.fx.global.domain.MealType
import com.fx.global.exception.NotificationException
import com.fx.global.exception.errorcode.NotificationErrorCode

@WebOutputAdapter
class NotificationWebAdapter(
    private val notificationFeignClient: NotificationFeignClient
) : NotificationWebPort {

    override fun notifyNotice(fcmToken: String, nttId: Long): Boolean {
        return try {
            val response = notificationFeignClient.notifyNotice(fcmToken, nttId)
            response.body?.metaData?.isSuccess ?: false
        } catch (e: Exception) {
            throw NotificationException(NotificationErrorCode.NOTIFICATION_SEND_FAILED)
        }
    }

    override fun notifyMeal(fcmToken: String, mealType: MealType): Boolean {
        return try {
            val response = notificationFeignClient.notifyMeal(fcmToken, mealType)
            response.body?.metaData?.isSuccess ?: false
        } catch (e: Exception) {
            throw NotificationException(NotificationErrorCode.NOTIFICATION_SEND_FAILED)
        }
    }
}