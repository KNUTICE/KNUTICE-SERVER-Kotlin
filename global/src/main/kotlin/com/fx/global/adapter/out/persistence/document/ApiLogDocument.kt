package com.fx.global.adapter.out.persistence.document

import com.fx.global.adapter.out.persistence.base.MongoBaseDocument
import com.fx.global.domain.ApiLog
import com.fx.global.domain.DeviceType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "api_log")
class ApiLogDocument(

    @Id
    var id: String? = null,

    var urlPattern: String,

    var url: String,

    var method: String,

    var queryParameters: Map<String, String>? = null,

    var fcmToken: String? = null,

    var clientIp: String,

    var deviceType: DeviceType,

    var statusCode: Int,

    var executionTime: Long

) : MongoBaseDocument() {

    fun toDomain(): ApiLog =
        ApiLog(
            id = id,
            urlPattern = urlPattern,
            url = url,
            method = method,
            queryParameters = queryParameters,
            fcmToken = fcmToken,
            clientIp = clientIp,
            deviceType = deviceType,
            statusCode = statusCode,
            executionTime = executionTime,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    companion object {
        fun from(apiLog: ApiLog): ApiLogDocument =
            ApiLogDocument(
                id = apiLog.id,
                urlPattern = apiLog.urlPattern,
                url = apiLog.url,
                method = apiLog.method,
                queryParameters = apiLog.queryParameters,
                fcmToken = apiLog.fcmToken,
                clientIp = apiLog.clientIp,
                deviceType = apiLog.deviceType,
                statusCode = apiLog.statusCode,
                executionTime = apiLog.executionTime
            ).apply {
                this.createdAt = apiLog.createdAt
                this.updatedAt = apiLog.updatedAt
            }
    }
}