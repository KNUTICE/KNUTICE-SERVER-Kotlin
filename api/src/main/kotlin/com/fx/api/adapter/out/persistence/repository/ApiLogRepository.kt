package com.fx.api.adapter.out.persistence.repository

import com.fx.api.domain.ApiLog
import org.springframework.data.jpa.repository.JpaRepository

interface ApiLogRepository : JpaRepository<ApiLog, Long>
