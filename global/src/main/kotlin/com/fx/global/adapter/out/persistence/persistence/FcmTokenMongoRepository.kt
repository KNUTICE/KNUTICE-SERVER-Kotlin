package com.fx.global.adapter.out.persistence.repository;

import com.fx.global.adapter.out.persistence.document.FcmTokenDocument
import com.fx.global.domain.DeviceType;
import org.springframework.data.mongodb.repository.MongoRepository

interface FcmTokenMongoRepository : MongoRepository<FcmTokenDocument, String> {

    fun countByIsActiveAndDeviceType(isActive: Boolean, deviceType: DeviceType): Long

}