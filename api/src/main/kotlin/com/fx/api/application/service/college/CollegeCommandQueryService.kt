package com.fx.api.application.service.college

import com.fx.api.application.port.`in`.college.CollegeCommandUseCase
import com.fx.api.application.port.`in`.college.CollegeQueryUseCase
import com.fx.api.application.port.`in`.college.dto.CollegeDisplayNameUpdateCommand
import com.fx.api.application.port.out.college.CollegePersistencePort
import com.fx.api.domain.CollegeDetail
import com.fx.common.application.port.`in`.CatalogQueryUseCase
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 단과대 관리자 조회 · 표시명 수정.
 *
 * 학과 수는 카탈로그 캐시(최대 5분 늦음)가 아니라 DB 에서 바로 센다. 관리자 화면은 방금 바꾼 값이 보여야 한다.
 */
@Service
class CollegeCommandQueryService(
    private val collegePersistencePort: CollegePersistencePort,
    private val catalogQueryUseCase: CatalogQueryUseCase,
) : CollegeQueryUseCase, CollegeCommandUseCase {

    @Transactional(readOnly = true)
    override fun getColleges(): List<CollegeDetail> {
        val topicCounts = collegePersistencePort.countTopicsByCollegeId()
        return collegePersistencePort.findAll().map {
            CollegeDetail(it, topicCounts[it.id] ?: 0L)
        }
    }

    /**
     * 트랜잭션을 걸지 않는다. 저장은 persistence port 가 자기 트랜잭션에서 커밋하고, 그 뒤에 카탈로그 캐시를 비운다.
     * 여기서 트랜잭션을 열면 커밋 전에 캐시를 비우게 되고, 그 사이 다른 요청이 옛 표시명을 다시 캐시할 수 있다.
     *
     * crawler 는 캐시를 따로 가지므로 알림 문구에는 TTL(5분) 안에 반영된다.
     */
    override fun updateDisplayName(command: CollegeDisplayNameUpdateCommand): CollegeDetail {
        val college = collegePersistencePort.updateDisplayName(command.collegeKey, command.displayName)
        catalogQueryUseCase.refresh()
        return CollegeDetail(college, collegePersistencePort.countTopicsByCollegeId()[college.id] ?: 0L)
    }

}
