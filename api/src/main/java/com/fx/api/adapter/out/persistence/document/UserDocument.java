package com.fx.api.adapter.out.persistence.document;

import com.fx.api.domain.Report;
import com.fx.api.domain.User;
import com.fx.api.domain.UserRole;
import com.fx.global.adapter.out.persistence.base.MongoBaseDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "user")
@SuperBuilder
@NoArgsConstructor
public class UserDocument extends MongoBaseDocument {

    @Id
    private String id;

    private String email;

    private String password;

    private String nickname;

    private UserRole role;

    public static UserDocument from(User user) {
        return UserDocument.builder()
            .id(user.getId())
            .email(user.getEmail())
            .password(user.getPassword())
            .nickname(user.getNickname())
            .role(user.getRole())
            .build();
    }

    public User toDomain() {
        return new User(
            this.getId(),
            this.email,
            this.password,
            this.nickname,
            this.role,
            this.createdAt,
            this.updatedAt
        );
    }

}
