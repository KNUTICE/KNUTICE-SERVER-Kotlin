package com.fx.global.adapter.out.persistence.repository;

import com.fx.global.adapter.out.persistence.document.FcmTokenDocument
import com.fx.global.domain.DeviceType;
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface FcmTokenMongoRepository : CoroutineCrudRepository<FcmTokenDocument, String> {

    suspend fun countByIsActiveAndDeviceType(isActive: Boolean, deviceType: DeviceType): Long

}