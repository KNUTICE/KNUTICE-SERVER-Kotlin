package com.fx.api.adapter.out.persistence.repository;

import com.fx.api.adapter.out.persistence.document.TipDocument
import com.fx.global.domain.DeviceType;
import org.springframework.data.mongodb.repository.MongoRepository

interface TipMongoRepository : MongoRepository<TipDocument, String> {

    fun findAllByDeviceTypeOrderByCreatedAtDesc(deviceType: DeviceType): List<TipDocument>

}
