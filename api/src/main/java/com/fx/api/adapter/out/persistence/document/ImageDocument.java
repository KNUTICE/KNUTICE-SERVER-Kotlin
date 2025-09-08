package com.fx.api.adapter.out.persistence.document;

import com.fx.api.domain.Image;
import com.fx.api.domain.ImageType;
import com.fx.api.domain.Report;
import com.fx.global.adapter.out.persistence.base.MongoBaseDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "image")
@SuperBuilder
@NoArgsConstructor
public class ImageDocument extends MongoBaseDocument {

    @Id
    private String id;

    private String imageUrl;

    private String originalName;

    private String serverName;

    private String extension;

    private ImageType type;

    public static ImageDocument from(Image image) {
        return ImageDocument.builder()
            .id(image.getId())
            .imageUrl(image.getImageUrl())
            .originalName(image.getOriginalName())
            .serverName(image.getServerName())
            .extension(image.getExtension())
            .type(image.getType())
            .createdAt(image.getCreatedAt())
            .updatedAt(image.getUpdatedAt())
            .build();
    }

    public Image toDomain() {
        return new Image(
            this.id,
            this.imageUrl,
            this.originalName,
            this.serverName,
            this.extension,
            this.type,
            this.createdAt,
            this.updatedAt
        );
    }
}
