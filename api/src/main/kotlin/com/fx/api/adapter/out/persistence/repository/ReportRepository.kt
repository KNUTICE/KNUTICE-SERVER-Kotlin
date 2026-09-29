package com.fx.api.adapter.out.persistence.repository

import com.fx.api.domain.Report
import org.springframework.data.jpa.repository.JpaRepository

interface ReportRepository : JpaRepository<Report, Long>
