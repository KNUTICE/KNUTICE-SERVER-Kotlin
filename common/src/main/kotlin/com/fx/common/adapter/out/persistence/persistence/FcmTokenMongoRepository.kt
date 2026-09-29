package com.fx.common.adapter.out.persistence.repository;

import com.fx.common.adapter.out.persistence.document.FcmTokenDocument
import com.fx.common.domain.DeviceType;
import org.springframework.data.mongodb.repository.MongoRepository

interface FcmTokenMongoRepository : MongoRepository<FcmTokenDocument, String> {

    fun countByIsActiveAndDeviceType(isActive: Boolean, deviceType: DeviceType): Long

}