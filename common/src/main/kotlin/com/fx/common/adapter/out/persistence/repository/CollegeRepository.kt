package com.fx.common.adapter.out.persistence.repository

import com.fx.common.domain.college.College
import org.springframework.data.jpa.repository.JpaRepository

interface CollegeRepository : JpaRepository<College, Long> {

    fun findAllByDeletedAtIsNull(): List<College>

}
