package com.fx.api.application.port.out.college

import com.fx.common.domain.college.College
import com.fx.common.domain.i18n.LocalizedText

interface CollegePersistencePort {

    /** 삭제되지 않은 단과대. 표시 순서대로. */
    fun findAll(): List<College>

    /** 단과대 id 별 삭제되지 않은 소속 학과 수. 학과가 없는 단과대는 빠진다. */
    fun countTopicsByCollegeId(): Map<Long, Long>

    /**
     * 표시명을 바꾸고 이 메서드의 트랜잭션에서 커밋한다. 돌아온 뒤에는 저장이 끝난 상태다.
     *
     * @throws com.fx.api.exception.CollegeException 없거나 삭제된 단과대 (COLLEGE_NOT_FOUND)
     */
    fun updateDisplayName(collegeKey: String, displayName: LocalizedText): College

}
