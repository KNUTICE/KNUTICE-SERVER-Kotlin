package com.fx.api.domain

import com.fx.common.domain.college.College

/** 단과대와 삭제되지 않은 소속 학과(MAJOR 토픽) 수. */
class CollegeDetail(
    val college: College,
    val topicCount: Long,
)
