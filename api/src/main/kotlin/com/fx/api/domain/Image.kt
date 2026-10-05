package com.fx.api.domain

import com.fx.persistence.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table

/** 서버에 저장한 이미지 파일 정보. 파일은 [serverName].[extension] 으로 저장된다. */
@Entity
@Table(
    name = "image",
    indexes = [
        // 유형별 조회 : `WHERE image_type = ?`
        Index(name = "idx_image_image_type", columnList = "image_type"),
    ],
)
class Image(
    imageUrl: String,
    originalName: String,
    serverName: String,
    extension: String,
    type: ImageType,
) : BaseEntity() {

    @Column(name = "image_url", nullable = false, length = 1000, comment = "이미지 URL")
    val imageUrl: String = imageUrl

    @Column(name = "original_name", nullable = false, length = 255, comment = "업로드 원본 파일명")
    val originalName: String = originalName

    @Column(name = "server_name", nullable = false, length = 100, comment = "서버 저장 파일명 (확장자 제외)")
    val serverName: String = serverName

    @Column(name = "extension", nullable = false, length = 20, comment = "확장자")
    val extension: String = extension

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false, length = 30, comment = "DEFAULT_IMAGE / TIP_IMAGE")
    val type: ImageType = type

}
