package com.fx.api.domain

import com.fx.common.domain.DeviceType
import com.fx.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table

/** 앱 사용 팁 링크. */
@Entity
@Table(
    name = "tip",
    indexes = [
        // 기기별 목록 : `WHERE device_type = ? ORDER BY id DESC`
        Index(name = "idx_tip_device_type_id", columnList = "device_type, id"),
    ],
)
class Tip(
    title: String,
    url: String,
    deviceType: DeviceType,
) : BaseEntity() {

    @Column(name = "title", nullable = false, length = 200, comment = "제목")
    val title: String = title

    @Column(name = "url", nullable = false, length = 1000, comment = "링크")
    val url: String = url

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 20, comment = "iOS / AOS / UNKNOWN")
    val deviceType: DeviceType = deviceType

}
