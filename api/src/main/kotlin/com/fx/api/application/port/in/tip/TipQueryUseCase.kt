package com.fx.api.application.port.`in`.tip

import com.fx.api.domain.Tip
import com.fx.common.domain.DeviceType

interface TipQueryUseCase {
    fun getTips(deviceType: DeviceType): List<Tip>
}