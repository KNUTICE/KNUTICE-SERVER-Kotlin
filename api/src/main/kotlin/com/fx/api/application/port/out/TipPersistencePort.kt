package com.fx.api.application.port.out

import com.fx.api.domain.Tip
import com.fx.common.domain.DeviceType

interface TipPersistencePort {

    fun save(tip: Tip): Tip

    fun deleteById(tipId: Long)

    /** 최근 등록 순. */
    fun findAllByDeviceType(deviceType: DeviceType): List<Tip>

}
