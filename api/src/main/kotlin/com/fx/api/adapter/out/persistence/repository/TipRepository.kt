package com.fx.api.adapter.out.persistence.repository

import com.fx.api.domain.Tip
import com.fx.common.domain.DeviceType
import org.springframework.data.jpa.repository.JpaRepository

interface TipRepository : JpaRepository<Tip, Long> {

    fun findAllByDeviceTypeOrderByIdDesc(deviceType: DeviceType): List<Tip>

}
