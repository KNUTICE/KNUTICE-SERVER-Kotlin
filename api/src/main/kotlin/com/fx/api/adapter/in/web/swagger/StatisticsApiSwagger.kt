package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.statistics.StatisticsResponse
import com.fx.api.adapter.`in`.web.dto.tip.TipSaveRequest
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "통계 관리 - ADMIN")
interface StatisticsApiSwagger {

    @Operation(summary = "통계 조회", description = "통계 정보를 조회합니다. <br>" +
            "`noticeCount`: 공지사항 총 개수 <br>" +
            "`noticeSummaryCount`: 공지사항 요약본 총 개수 <br>" +
            "`fcmTokenCount`: FCM 토큰 총 개수 - 비활성 토큰 개수를 포함합니다.<br>" +
            "`aosActiveFcmTokenCount`: AOS 활성 FCM 토큰 개수 <br>" +
            "`iosActiveFcmTokenCount`: iOS 활성 FCM 토큰 개수")
    suspend fun getStatistics(): ResponseEntity<Api<StatisticsResponse>>

}