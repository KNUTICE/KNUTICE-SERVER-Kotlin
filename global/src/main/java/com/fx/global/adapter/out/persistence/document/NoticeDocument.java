package com.fx.global.adapter.out.persistence.document;

import com.fx.global.adapter.out.persistence.base.MongoBaseDocument;
import com.fx.global.domain.CrawlableType;
import com.fx.global.domain.Notice;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "notice")
@SuperBuilder
@NoArgsConstructor
public class NoticeDocument extends MongoBaseDocument {

    @Id
    private Long nttId;

    private String title;

    private String department;

    private String contentUrl;

    private String contentImageUrl;

    private LocalDate registrationDate;

    private Boolean isAttachment;

    private String type;

    public Notice toDomain() {
        return new Notice(
            this.nttId,
            this.title,
            this.department,
            this.contentUrl,
            this.contentImageUrl,
            this.registrationDate,
            this.isAttachment,
            CrawlableType.fromString(this.type),
            this.createdAt,
            this.updatedAt
        );
    }

    public static NoticeDocument from(Notice notice) {
        return NoticeDocument.builder()
            .nttId(notice.getNttId())
            .title(notice.getTitle())
            .department(notice.getDepartment())
            .contentImageUrl(notice.getContentUrl())
            .registrationDate(notice.getRegistrationDate())
            .isAttachment(notice.isAttachment())
            .type(notice.getType().getTypeName())
            .createdAt(notice.getCreatedAt())
            .build();
    }

}
