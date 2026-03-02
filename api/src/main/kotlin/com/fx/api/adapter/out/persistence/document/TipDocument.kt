package com.fx.api.adapter.out.persistence.document

import com.fx.api.domain.Tip
import com.fx.global.adapter.out.persistence.base.MongoBaseDocument
import com.fx.global.domain.DeviceType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "tip")
class TipDocument(

    @Id
    var id: String? = null,

    var title: String,

    var url: String,

    var deviceType: DeviceType

) : MongoBaseDocument() {

    fun toDomain(): Tip =
        Tip(
            id = id,
            title = title,
            url = url,
            deviceType = deviceType,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    companion object {
        fun from(tip: Tip): TipDocument =
            TipDocument(
                id = tip.id,
                title = tip.title,
                url = tip.url,
                deviceType = tip.deviceType
            ).apply {
                this.createdAt = tip.createdAt
                this.updatedAt = tip.updatedAt
            }
    }
}