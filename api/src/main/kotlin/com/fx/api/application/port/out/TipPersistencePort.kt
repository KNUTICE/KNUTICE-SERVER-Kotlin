package com.fx.api.application.port.out

import com.fx.api.domain.Tip
import com.fx.global.domain.DeviceType

interface TipPersistencePort {

    suspend fun saveTip(tip: Tip)
    suspend fun deleteById(tipId: String)
    suspend fun getTips(deviceType: DeviceType): List<Tip>

}