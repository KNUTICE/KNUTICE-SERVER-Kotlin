package com.fx.api.adapter.out.persistence.repository;

import com.fx.api.adapter.out.persistence.document.TipDocument
import com.fx.global.domain.DeviceType;
import kotlinx.coroutines.flow.Flow
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.util.List;

interface TipMongoRepository : CoroutineCrudRepository<TipDocument, String> {

    fun findAllByDeviceTypeOrderByCreatedAtDesc(deviceType: DeviceType): Flow<TipDocument>

}
