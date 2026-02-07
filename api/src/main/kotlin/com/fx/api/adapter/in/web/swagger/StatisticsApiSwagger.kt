package com.fx.api.adapter.`in`.web.swagger

import com.fx.api.adapter.`in`.web.dto.statistics.ApiLogStatisticsResponse
import com.fx.api.adapter.`in`.web.dto.statistics.StatisticsResponse
import com.fx.api.adapter.`in`.web.dto.statistics.TopicCountStatisticsResponse
import io.github.seob7.Api
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

@Tag(name = "통계 관리 - ADMIN")
interface StatisticsApiSwagger {

    @Operation(
        summary = "일일 통계 조회",
        description = """일일 통계 정보를 페이징하여 조회합니다. 
오늘 날짜의 데이터는 실시간 집계되어 결과의 첫 번째 요소로 포함됩니다.

### 페이징 방법
- **첫 요청** : `cursorDate`를 비우고 호출 (오늘 포함 최신 10개 반환)
- **다음 요청** : 이전 응답에서 받은 마지막 `statisticsDate`를 `cursorDate`에 입력
- **중복 방지** : 입력한 `cursorDate`는 결과에서 제외되고, 그 **이전(미만)** 날짜부터 조회됩니다.

### 응답 필드 설명
- **statisticsDate** : 통계 기준 날짜 (yyyy-MM-dd)
- **noticeCount** : 전체 공지사항 수
- **noticeSummaryCount** : 요약 공지사항 수
- **fcmTokenActiveCount** : 활성 토큰 수
- **fcmTokenActiveAosCount** : AOS 활성 토큰 수
- **fcmTokenActiveIosCount** : iOS 활성 토큰 수
- **fcmTokenInactiveCount** : 총 비활성 토큰 수
- **fcmTokenInactiveAosCount** : AOS 비활성 토큰 수
- **fcmTokenInactiveIosCount** : iOS 비활성 토큰 수"""
    )
    suspend fun getDailyStatistics(
        @Parameter(
            name = "cursorDate",
            description = "조회 기준 날짜 커서, 20XX-XX-XX 형식 (해당 날짜 미만의 과거 데이터 조회)",
            `in` = ParameterIn.QUERY
        )
        @RequestParam(required = false) cursorDate: LocalDate?,

        @Parameter(
            name = "size",
            description = "조회할 데이터 개수 (기본값: 10)",
            `in` = ParameterIn.QUERY
        )
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Api<List<StatisticsResponse>>>

    @Operation(
        summary = "API 로그 통계 조회",
        description = """API 호출 로그 통계를 조회합니다.
### 페이징 방법
- **첫 요청** : `cursorDate`를 비우고 호출 (오늘 미만 최신 10개 반환)
- **다음 요청** : 이전 응답에서 받은 마지막 `statisticsDate`를 `cursorDate`에 입력
- **중복 방지** : 입력한 `cursorDate`는 결과에서 제외되고, 그 **이전(미만)** 날짜부터 조회됩니다

### 응답 필드 설명
- **statisticsDate** : 통계 기준 날짜 (yyyy-MM-dd)
- **urlPattern** : API 패턴
- **method** : HTTP 메서드
- **totalCount** : 호출 총 횟수
- **errorCount** : 에러 발생 횟수
- **averageExecutionTime** : 평균 실행 시간"""
    )
    suspend fun getApiLogStatistics(
        @Parameter(
            name = "cursorDate",
            description = "조회 기준 날짜 커서, 20XX-XX-XX 형식 (해당 날짜 미만의 과거 데이터 조회)",
            `in` = ParameterIn.QUERY
        )
        @RequestParam(required = false) cursorDate: LocalDate?,

        @Parameter(
            name = "size",
            description = "조회할 데이터 개수 (기본값: 10)",
            `in` = ParameterIn.QUERY
        )
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Api<List<ApiLogStatisticsResponse>>>

    @Operation(
        summary = "주제별 통계 조회",
        description = """공지사항 주제별 통계를 조회합니다.
 ### 페이징 방법
- **첫 요청** : `cursorDate`를 비우고 호출 (오늘 미만 최신 10개 반환)
- **다음 요청** : 이전 응답에서 받은 마지막 `statisticsDate`를 `cursorDate`에 입력
- **중복 방지** : 입력한 `cursorDate`는 결과에서 제외되고, 그 **이전(미만)** 날짜부터 조회됩니다

### 응답 필드 설명
- **date** : 통계 기준 날짜 (yyyy-MM-dd)
- **topic** : 주제명
- **count** : 해당 주제의 통계 수"""
    )
    suspend fun getTopicStatistics(
        @Parameter(
            name = "cursorDate",
            description = "조회 기준 날짜 커서, 20XX-XX-XX 형식 (해당 날짜 미만의 과거 데이터 조회)",
            `in` = ParameterIn.QUERY
        )
        @RequestParam(required = false) cursorDate: LocalDate?,

        @Parameter(
            name = "size",
            description = "조회할 데이터 개수 (기본값: 10)",
            `in` = ParameterIn.QUERY
        )
        @RequestParam(defaultValue = "10") size: Int
    ): ResponseEntity<Api<List<TopicCountStatisticsResponse>>>
}