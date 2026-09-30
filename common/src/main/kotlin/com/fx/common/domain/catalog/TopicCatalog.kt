package com.fx.common.domain.catalog

import com.fx.common.domain.TopicType
import com.fx.common.domain.i18n.LocalizedText

/** 카탈로그에 담는 단과대 읽기 모델. */
data class CollegeView(
    val id: Long,
    val collegeKey: String,
    val displayName: LocalizedText,
    val displayOrder: Int,
)

/** 카탈로그에 담는 토픽 읽기 모델. 소속 단과대를 함께 담아 표시명을 JOIN 없이 얻는다. */
data class TopicView(
    val id: Long,
    val code: Int,
    val name: String,
    val topicType: TopicType,
    val displayName: LocalizedText,
    val college: CollegeView?,
    val rootDomain: String,
    val bbsPath: String,
    val crawlEnabled: Boolean,
    val visible: Boolean,
) {

    /** 크롤링할 게시판 목록 URL. */
    fun noticeUrl(): String =
        rootDomain + bbsPath

}

/**
 * 삭제되지 않은 토픽 · 단과대 전체의 불변 스냅샷.
 *
 * 공지 · 구독은 `topic_code` 만 저장하므로 표시명 · 단과대 · 크롤링 정보는 여기서 찾는다.
 * v1 API 는 [findByName], v2 API 는 [findByCode] 로 조회한다.
 */
class TopicCatalog(
    topics: List<TopicView>,
    colleges: List<CollegeView>,
) {

    /** code 오름차순. 레거시 enum 선언 순서와 같다. */
    val topics: List<TopicView> =
        topics.sortedBy {
            it.code
        }

    /** 표시 순서 오름차순. */
    val colleges: List<CollegeView> =
        colleges.sortedBy {
            it.displayOrder
        }

    private val topicsByCode: Map<Int, TopicView> =
        this.topics.associateBy {
            it.code
        }
    private val topicsByName: Map<String, TopicView> =
        this.topics.associateBy {
            it.name
        }

    fun findByCode(code: Int): TopicView? =
        topicsByCode[code]

    fun findByName(name: String): TopicView? =
        topicsByName[name]

    fun topicsOf(topicType: TopicType): List<TopicView> =
        topics.filter {
            it.topicType == topicType
        }

}
