package com.fx.api.application.port.out

import com.fx.api.domain.Tip
import com.fx.global.domain.DeviceType

interface TipPersistencePort {

    fun saveTip(tip: Tip)
    fun deleteById(tipId: String)
    fun getTips(deviceType: DeviceType): List<Tip>

}