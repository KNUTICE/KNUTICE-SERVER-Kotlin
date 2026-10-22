package com.fx.api.application.port.out.college

import com.fx.common.domain.college.College

interface CollegePersistencePort {

    /** 삭제되지 않은 단과대. 표시 순서대로. */
    fun findAll(): List<College>

    /** 단과대 id 별 삭제되지 않은 소속 학과 수. 학과가 없는 단과대는 빠진다. */
    fun countTopicsByCollegeId(): Map<Long, Long>

}
