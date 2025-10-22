package com.fx.api.application.port.out

interface NotificationWebPort {

    fun notifyNotice(fcmToken: String, nttId: Long): Boolean

}