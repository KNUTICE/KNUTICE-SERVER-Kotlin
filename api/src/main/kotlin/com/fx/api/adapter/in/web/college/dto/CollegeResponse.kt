package com.fx.api.adapter.`in`.web.college.dto

import com.fx.api.domain.CollegeDetail
import com.fx.common.domain.i18n.LocalizedText

data class CollegeResponse(
    val collegeKey: String,
    val displayName: DisplayNameResponse,
    val displayOrder: Int,
    /** 삭제되지 않은 소속 학과 수 */
    val topicCount: Long,
) {

    /** `ko` 는 항상 있고 `en` · `ja` 는 null 일 수 있다. */
    data class DisplayNameResponse(
        val ko: String,
        val en: String?,
        val ja: String?,
    ) {

        companion object {

            fun from(text: LocalizedText): DisplayNameResponse =
                DisplayNameResponse(text.ko, text.en, text.ja)

        }

    }

    companion object {

        fun from(detail: CollegeDetail): CollegeResponse {
            val college = detail.college
            return CollegeResponse(
                collegeKey = college.collegeKey,
                displayName = DisplayNameResponse.from(college.displayName),
                displayOrder = college.displayOrder,
                topicCount = detail.topicCount,
            )
        }

        fun from(details: List<CollegeDetail>): List<CollegeResponse> =
            details.map {
                from(it)
            }

    }

}
