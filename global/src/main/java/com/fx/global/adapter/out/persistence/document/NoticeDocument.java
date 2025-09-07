package com.fx.global.adapter.out.persistence.document;

import com.fx.global.adapter.out.persistence.base.MongoBaseDocument;
import com.fx.global.domain.CrawlableType;
import com.fx.global.domain.Notice;
import com.querydsl.core.annotations.QueryEntity;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "notice")
@SuperBuilder
@NoArgsConstructor
@QueryEntity
public class NoticeDocument extends MongoBaseDocument {

    @Id
    private Long nttId;

    private String title;

    private String department;

    private String contentUrl;

    private String contentImageUrl;

    private LocalDate registrationDate;

    private Boolean isAttachment;

    private String topic;

    public Notice toDomain() {
        return new Notice(
            this.nttId,
            this.title,
            this.department,
            this.contentUrl,
            this.contentImageUrl,
            this.registrationDate,
            this.isAttachment,
            CrawlableType.fromString(this.topic),
            this.createdAt,
            this.updatedAt
        );
    }

    public static NoticeDocument from(Notice notice) {
        return NoticeDocument.builder()
            .nttId(notice.getNttId())
            .title(notice.getTitle())
            .department(notice.getDepartment())
            .contentUrl(notice.getContentUrl())
            .contentImageUrl(notice.getContentImageUrl())
            .registrationDate(notice.getRegistrationDate())
            .isAttachment(notice.isAttachment())
            .topic(notice.getTopic().getTopicName())
            .createdAt(notice.getCreatedAt())
            .build();
    }

}
