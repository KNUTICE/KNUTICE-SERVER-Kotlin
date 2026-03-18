package com.fx.api.adapter.out.persistence.repository;

import com.fx.api.adapter.out.persistence.document.TipDocument
import com.fx.global.domain.DeviceType;
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.List;

interface TipMongoRepository : CoroutineCrudRepository<TipDocument, String> {

    suspend fun findAllByDeviceTypeOrderByCreatedAtDesc(deviceType: DeviceType): List<TipDocument>

}
