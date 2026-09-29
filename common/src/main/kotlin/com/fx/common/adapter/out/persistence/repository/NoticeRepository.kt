package com.fx.common.adapter.out.persistence.repository

import com.fx.common.domain.notice.Notice
import org.springframework.data.jpa.repository.JpaRepository

interface NoticeRepository : JpaRepository<Notice, Long> {

    fun findByNttId(nttId: Long): Notice?

    fun existsByNttId(nttId: Long): Boolean

}
