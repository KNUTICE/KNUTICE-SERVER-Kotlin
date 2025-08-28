package adapter.out.persistence.base;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@SuperBuilder
@NoArgsConstructor
public class MongoBaseRecord {

    @CreatedDate
    protected LocalDateTime createdAt = null;

    @LastModifiedDate
    protected LocalDateTime updatedAt = null;

}
