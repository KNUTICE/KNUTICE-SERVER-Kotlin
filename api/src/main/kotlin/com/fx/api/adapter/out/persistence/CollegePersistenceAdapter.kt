package com.fx.api.adapter.out.persistence

import com.fx.api.adapter.out.persistence.repository.CollegeQueryRepository
import com.fx.api.application.port.out.college.CollegePersistencePort
import com.fx.api.exception.CollegeException
import com.fx.api.exception.errorcode.CollegeErrorCode
import com.fx.common.adapter.out.persistence.repository.CollegeRepository
import com.fx.common.annotation.PersistenceAdapter
import com.fx.common.domain.college.College
import com.fx.common.domain.i18n.LocalizedText
import org.springframework.transaction.annotation.Transactional

@PersistenceAdapter
class CollegePersistenceAdapter(
    private val collegeRepository: CollegeRepository,
    private val collegeQueryRepository: CollegeQueryRepository,
) : CollegePersistencePort {

    override fun findAll(): List<College> =
        collegeRepository.findAllByDeletedAtIsNullOrderByDisplayOrderAsc()

    override fun countTopicsByCollegeId(): Map<Long, Long> =
        collegeQueryRepository.countTopicsByCollegeId()

    /** 서비스가 커밋 뒤에 카탈로그 캐시를 비울 수 있도록, 트랜잭션을 여기서 열고 닫는다. */
    @Transactional
    override fun updateDisplayName(collegeKey: String, displayName: LocalizedText): College {
        val college = collegeRepository.findByCollegeKeyAndDeletedAtIsNull(collegeKey)
            ?: throw CollegeException(CollegeErrorCode.COLLEGE_NOT_FOUND)
        college.changeDisplayName(displayName)
        return college
    }

}
