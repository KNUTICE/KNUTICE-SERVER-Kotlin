package com.fx.crawler.appllication.service

import com.fx.crawler.appllication.port.out.NoticePersistencePort
import com.fx.crawler.appllication.port.out.NoticeSummaryPort
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class NoticeSummaryService(
    private val noticePersistencePort: NoticePersistencePort,
    private val noticeSummaryPort: NoticeSummaryPort
) {
    private val log = LoggerFactory.getLogger(NoticeSummaryService::class.java)

    suspend fun summarizeAllMissing() {
        var totalProcessed = 0
        val batchSize = 5

        while (true) {
            // 1. 요약이 필요한 데이터 5개 가져오기 (최신순)
            val targets = noticePersistencePort.findByContentSummaryIsNullOrderByCreatedAtDesc(batchSize)

            if (targets.isEmpty()) {
                log.info("요약할 데이터가 더 이상 없습니다. 작업을 종료합니다.")
                break
            }

            log.info("${targets.size}개의 데이터 요약 시작... (누적 처리: $totalProcessed)")

            try {
                // 2. AI 요약 수행 (GeminiAdapter 호출)
                // GeminiAdapter 내부에서 이미 비동기(async)로 5개를 동시에 처리함
                val summarizedNotices = noticeSummaryPort.summarizeNotices(targets)

                if (summarizedNotices.isNotEmpty()) {
                    // 3. 결과 저장
                    noticePersistencePort.saveAll(summarizedNotices)
                    totalProcessed += summarizedNotices.size
                    log.info("${summarizedNotices.size}개 저장 완료.")
                }

                // 4. Rate Limit를 위한 3초 대기
                log.info("Rate limit 방지를 위해 3초간 대기합니다...")
                delay(3000)

            } catch (e: Exception) {
                log.error("배치 처리 중 오류 발생: ${e.message}", e)
                // 오류 발생 시 잠시 더 쉬었다가 다음 배치를 시도하거나 멈춤
                delay(10000)
            }
        }

        log.info("전체 요약 작업 완료. 총 처리 건수: $totalProcessed")
    }
}