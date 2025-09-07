package com.fx.api.application.port.out

import com.fx.api.domain.PushNotice

interface NotificationWebPort {

    fun pushTestNotice(pushNotice: PushNotice): Boolean

}