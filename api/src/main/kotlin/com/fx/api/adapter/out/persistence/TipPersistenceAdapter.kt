package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.document.TipDocument
import com.fx.api.adapter.out.persistence.repository.TipMongoRepository
import com.fx.api.application.port.out.TipPersistencePort
import com.fx.api.domain.Tip
import com.fx.global.annotation.PersistenceAdapter
import com.fx.global.domain.DeviceType

@PersistenceAdapter
class TipPersistenceAdapter(
    private val tipMongoRepository: TipMongoRepository
) : TipPersistencePort {

    override fun saveTip(tip: Tip) {
        tipMongoRepository.save(TipDocument.from(tip))
    }

    override fun deleteById(tipId: String) {
        tipMongoRepository.deleteById(tipId)
    }

    override fun getTips(deviceType: DeviceType): List<Tip> =
        tipMongoRepository.findAllByDeviceTypeOrderByCreatedAtDesc(deviceType).map { it.toDomain() }
}