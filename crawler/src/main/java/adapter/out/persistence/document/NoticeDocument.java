package adapter.out.persistence.document;

import adapter.out.persistence.base.MongoBaseRecord;
import com.fx.crawler.domain.CrawlableType;
import com.fx.crawler.domain.Notice;
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
public class NoticeDocument extends MongoBaseRecord {

    @Id
    private Long nttId;

    private String title;

    private String department;

    private String contentUrl;

    private String contentImage;

    private LocalDate registrationDate;

    private Boolean isAttachment;

    private String type;

    public Notice toDomain() {
        return new Notice(
            this.nttId,
            this.title,
            this.department,
            this.contentUrl,
            this.contentImage,
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
            .contentUrl(notice.getContentUrl())
            .registrationDate(notice.getRegistrationDate())
            .isAttachment(notice.isAttachment())
            .type(notice.getType().getTypeName())
            .build();
    }

}
