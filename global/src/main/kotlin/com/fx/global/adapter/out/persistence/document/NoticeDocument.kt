package com.fx.global.adapter.out.persistence.document

import com.fx.global.adapter.out.persistence.document.base.MongoBaseDocument
import com.fx.global.domain.CrawlableType
import com.fx.global.domain.Notice
import com.querydsl.core.annotations.QueryEntity
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate

@Document(collection = "notice")
@QueryEntity
class NoticeDocument(

    @Id
    var nttId: Long,

    var title: String,

    var department: String,

    var contentUrl: String,

    var content: String? = null,

    var contentSummary: String? = null,

    var contentImageUrl: String? = null,

    var registrationDate: LocalDate,

    var isAttachment: Boolean = false,

    var topic: String

) : MongoBaseDocument() {

    fun toDomain(): Notice =
        Notice(
            nttId = nttId,
            title = title,
            department = department,
            contentUrl = contentUrl,
            content = content,
            contentSummary = contentSummary,
            contentImageUrl = contentImageUrl,
            registrationDate = registrationDate,
            isAttachment = isAttachment,
            topic = CrawlableType.fromString(topic),
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    companion object {
        fun from(notice: Notice): NoticeDocument =
            NoticeDocument(
                nttId = notice.nttId,
                title = notice.title,
                department = notice.department,
                contentUrl = notice.contentUrl,
                content = notice.content,
                contentSummary = notice.contentSummary,
                contentImageUrl = notice.contentImageUrl,
                registrationDate = notice.registrationDate,
                isAttachment = notice.isAttachment,
                topic = notice.topic.topicName
            ).apply {
                this.createdAt = notice.createdAt
                this.updatedAt = notice.updatedAt
            }
    }

}