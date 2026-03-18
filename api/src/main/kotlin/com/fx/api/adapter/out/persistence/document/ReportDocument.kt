package com.fx.api.adapter.out.persistence.document

import com.fx.api.domain.Report
import com.fx.global.adapter.out.persistence.document.base.MongoBaseDocument
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "report")
class ReportDocument(

    @Id
    var id: String? = null,

    var fcmToken: String,

    var content: String,

    var deviceName: String,

    var version: String

) : MongoBaseDocument() {

    fun toDomain(): Report =
        Report(
            id = id,
            fcmToken = fcmToken,
            content = content,
            deviceName = deviceName,
            version = version,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    companion object {
        fun from(report: Report): ReportDocument =
            ReportDocument(
                id = report.id,
                fcmToken = report.fcmToken,
                content = report.content,
                deviceName = report.deviceName,
                version = report.version
            ).apply {
                this.createdAt = report.createdAt
                this.updatedAt = report.updatedAt
            }
    }
}