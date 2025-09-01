package com.fx.api.adapter.out.persistence.document;

import com.fx.api.domain.Tip;
import com.fx.global.adapter.out.persistence.base.MongoBaseDocument;
import com.fx.global.domain.DeviceType;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "tip")
@SuperBuilder
@NoArgsConstructor
public class TipDocument extends MongoBaseDocument {

    @Id
    private String id;

    private String title;

    private String url;

    private DeviceType deviceType;

    public static TipDocument from(Tip tip) {
        return TipDocument.builder()
            .id(tip.getId())
            .title(tip.getTitle())
            .url(tip.getUrl())
            .deviceType(tip.getDeviceType())
            .build();
    }

    public Tip toDomain() {
        return new Tip(
            this.id,
            this.title,
            this.url,
            this.deviceType,
            this.createdAt,
            this.updatedAt
        );
    }

}
