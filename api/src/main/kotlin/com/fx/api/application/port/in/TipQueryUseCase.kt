package com.fx.api.application.port.`in`

import com.fx.api.domain.Tip
import com.fx.global.domain.DeviceType

interface TipQueryUseCase {
    fun getTips(deviceType: DeviceType): List<Tip>
}