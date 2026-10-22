package com.fx.api.adapter.`in`.web.college.dto

import com.fx.api.application.port.`in`.college.dto.CollegeDisplayNameUpdateCommand
import com.fx.common.domain.college.COLLEGE_DISPLAY_NAME_MAX_LENGTH
import com.fx.common.domain.i18n.LocalizedText
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/** 표시명을 언어별로 통째로 바꾼다. */
data class CollegeDisplayNameUpdateRequest(

    @field:NotNull(message = "표시명은 필수입니다.")
    @field:Valid
    val displayName: DisplayNameRequest?,

) {

    data class DisplayNameRequest(

        @field:NotBlank(message = "한국어 표시명은 비워 둘 수 없습니다.")
        @field:Size(max = COLLEGE_DISPLAY_NAME_MAX_LENGTH, message = "표시명은 {max}자 이하여야 합니다.")
        val ko: String?,

        /** null 이나 빈 문자열이면 비운다. */
        @field:Size(max = COLLEGE_DISPLAY_NAME_MAX_LENGTH, message = "표시명은 {max}자 이하여야 합니다.")
        val en: String? = null,

        /** null 이나 빈 문자열이면 비운다. */
        @field:Size(max = COLLEGE_DISPLAY_NAME_MAX_LENGTH, message = "표시명은 {max}자 이하여야 합니다.")
        val ja: String? = null,

    )

    fun toCommand(collegeKey: String): CollegeDisplayNameUpdateCommand {
        val displayName = requireNotNull(displayName)
        return CollegeDisplayNameUpdateCommand(
            collegeKey = collegeKey,
            displayName = LocalizedText(
                ko = requireNotNull(displayName.ko),
                en = displayName.en?.takeIf(String::isNotBlank),
                ja = displayName.ja?.takeIf(String::isNotBlank),
            ),
        )
    }

}
