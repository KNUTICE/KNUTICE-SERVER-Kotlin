package com.fx.api.application.service.college

import com.fx.api.application.port.`in`.college.CollegeQueryUseCase
import com.fx.api.application.port.out.college.CollegePersistencePort
import com.fx.api.domain.CollegeDetail
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 단과대 관리자 조회.
 *
 * 학과 수는 카탈로그 캐시(최대 5분 늦음)가 아니라 DB 에서 바로 센다. 관리자 화면은 방금 바꾼 값이 보여야 한다.
 */
@Service
class CollegeCommandQueryService(
    private val collegePersistencePort: CollegePersistencePort,
) : CollegeQueryUseCase {

    @Transactional(readOnly = true)
    override fun getColleges(): List<CollegeDetail> {
        val topicCounts = collegePersistencePort.countTopicsByCollegeId()
        return collegePersistencePort.findAll().map {
            CollegeDetail(it, topicCounts[it.id] ?: 0L)
        }
    }

}
