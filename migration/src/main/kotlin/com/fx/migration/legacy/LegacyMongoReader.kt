package com.fx.migration.legacy

import com.fx.migration.MigrationProperties
import com.mongodb.client.model.Sorts
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

/** 레거시 컬렉션 이름. */
object LegacyCollections {
    const val NOTICE = "notice"
    const val FCM_TOKEN = "fcm_token"
    const val USER = "user"
    const val TIP = "tip"
    const val IMAGE = "image"
    const val REPORT = "report"

    /** 이관하지 않는 컬렉션. 건수만 리포트에 남긴다 */
    val EXCLUDED = listOf("api_log", "daily_statistics", "daily_api_log_statistics", "seat_alert")
}

/** 레거시 컬렉션을 원본 도큐먼트 그대로 읽는다. 쓰지 않는다. */
@Component
class LegacyMongoReader(
    private val mongoTemplate: MongoTemplate,
    private val properties: MigrationProperties,
) {

    fun count(collection: String): Long =
        if (mongoTemplate.collectionExists(collection)) mongoTemplate.getCollection(collection).countDocuments() else 0

    /**
     * 컬렉션을 `_id` 오름차순으로 [MigrationProperties.chunkSize] 개씩 넘겨준다.
     * `ObjectId` 는 생성 시각 순이므로 레거시 생성 순서대로 읽힌다.
     */
    fun forEachChunk(collection: String, block: (List<LegacyDocument>) -> Unit) {
        if (!mongoTemplate.collectionExists(collection)) {
            return
        }
        val chunkSize = properties.chunkSize
        mongoTemplate.getCollection(collection)
            .find()
            .sort(Sorts.ascending("_id"))
            .batchSize(chunkSize)
            .iterator()
            .use { cursor ->
                val chunk = ArrayList<LegacyDocument>(chunkSize)
                while (cursor.hasNext()) {
                    chunk += LegacyDocument(cursor.next(), properties.legacyZone)
                    if (chunk.size == chunkSize) {
                        block(chunk.toList())
                        chunk.clear()
                    }
                }
                if (chunk.isNotEmpty()) {
                    block(chunk.toList())
                }
            }
    }

}
