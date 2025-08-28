package adapter.out.persistence.base;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@SuperBuilder
@NoArgsConstructor
public class MongoBaseDocument {

    @CreatedDate
    @Builder.Default
    protected LocalDateTime createdAt = null;

    @LastModifiedDate
    @Builder.Default
    protected LocalDateTime updatedAt = null;

}
