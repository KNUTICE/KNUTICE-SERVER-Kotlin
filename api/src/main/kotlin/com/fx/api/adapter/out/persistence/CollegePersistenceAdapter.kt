package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.CollegeQueryRepository
import com.fx.api.application.port.out.college.CollegePersistencePort
import com.fx.common.adapter.out.persistence.repository.CollegeRepository
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.domain.college.College

@PersistenceAdapter
class CollegePersistenceAdapter(
    private val collegeRepository: CollegeRepository,
    private val collegeQueryRepository: CollegeQueryRepository,
) : CollegePersistencePort {

    override fun findAll(): List<College> =
        collegeRepository.findAllByDeletedAtIsNullOrderByDisplayOrderAsc()

    override fun countTopicsByCollegeId(): Map<Long, Long> =
        collegeQueryRepository.countTopicsByCollegeId()

}
