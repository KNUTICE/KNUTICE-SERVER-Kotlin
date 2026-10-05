package com.fx.common.domain.college

import com.fx.common.domain.i18n.LocalizedText
import com.fx.persistence.BaseEntity
import jakarta.persistence.AttributeOverride
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

/** 단과대 키 최대 길이 — `college_key` 컬럼 길이와 같아야 한다. */
const val COLLEGE_KEY_MAX_LENGTH = 50

/** 단과대 표시명 최대 길이 — `display_name_*` 컬럼 길이와 같아야 한다. */
const val COLLEGE_DISPLAY_NAME_MAX_LENGTH = 100

/**
 * 단과대. 학과(MAJOR) 토픽이 소속된다.
 *
 * [collegeKey] 는 생성 후 바꿀 수 없다 (레거시 `MajorType.college` 문자열 키를 그대로 쓴다).
 * 삭제는 논리 삭제이며, 소속 토픽이 남아 있으면 삭제하지 않는다 (확인은 유스케이스에서 한다).
 */
@Entity
@Table(
    name = "college",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_college_college_key", columnNames = ["college_key"]),
    ],
)
class College(
    collegeKey: String,
    displayName: LocalizedText,
    displayOrder: Int,
) : BaseEntity() {

    @Column(name = "college_key", nullable = false, updatable = false, length = COLLEGE_KEY_MAX_LENGTH, comment = "단과대 키 (생성 후 변경 불가)")
    val collegeKey: String = collegeKey

    @Embedded
    @AttributeOverride(name = "ko", column = Column(name = "display_name_ko", nullable = false, length = COLLEGE_DISPLAY_NAME_MAX_LENGTH, comment = "표시명 (한국어)"))
    @AttributeOverride(name = "en", column = Column(name = "display_name_en", nullable = true, length = COLLEGE_DISPLAY_NAME_MAX_LENGTH, comment = "표시명 (영어)"))
    @AttributeOverride(name = "ja", column = Column(name = "display_name_ja", nullable = true, length = COLLEGE_DISPLAY_NAME_MAX_LENGTH, comment = "표시명 (일본어)"))
    var displayName: LocalizedText = displayName
        protected set

    @Column(name = "display_order", nullable = false, comment = "표시 순서")
    var displayOrder: Int = displayOrder
        protected set

    @Column(name = "deleted_at", nullable = true, comment = "삭제 시각")
    var deletedAt: LocalDateTime? = null
        protected set

    init {
        require(collegeKey.isNotBlank()) {
            "단과대 키는 비어 있을 수 없습니다."
        }
    }

    val isDeleted: Boolean
        get() = deletedAt != null

    fun changeDisplayName(displayName: LocalizedText) {
        this.displayName = displayName
    }

    fun changeDisplayOrder(displayOrder: Int) {
        this.displayOrder = displayOrder
    }

    fun delete(deletedAt: LocalDateTime) {
        this.deletedAt = deletedAt
    }

}
