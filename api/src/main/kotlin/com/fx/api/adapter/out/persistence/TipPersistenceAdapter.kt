package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.TipRepository
import com.fx.api.application.port.out.TipPersistencePort
import com.fx.api.domain.Tip
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.domain.DeviceType

@PersistenceAdapter
class TipPersistenceAdapter(
    private val tipRepository: TipRepository,
) : TipPersistencePort {

    override fun save(tip: Tip): Tip = tipRepository.save(tip)

    override fun deleteById(tipId: Long) = tipRepository.deleteById(tipId)

    override fun findAllByDeviceType(deviceType: DeviceType): List<Tip> =
        tipRepository.findAllByDeviceTypeOrderByIdDesc(deviceType)

}
