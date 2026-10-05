package com.fx.crawler.application.port.`in`

interface SeatAlertCheckUseCase {

    /** 만료된 알림을 지우고 지운 수를 돌려준다. */
    fun deleteExpired(): Int

    /** 알림이 걸린 좌석이 비었는지 확인해 알림을 보내고 지운다. 보낸 수를 돌려준다. */
    fun checkAndNotify(): Int

}
