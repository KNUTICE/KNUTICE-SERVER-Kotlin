package com.fx.common.domain.batch

import org.springframework.scheduling.support.CronExpression
import java.time.LocalDateTime

/**
 * 배치 스케줄의 cron (Spring 6필드, Asia/Seoul).
 *
 * 폴러가 60초마다 스케줄을 확인하므로 초 필드는 `0` 만 허용한다. 분 단위보다 촘촘한 스케줄은 만들 수 없다.
 */
class BatchCron private constructor(
    val expression: String,
    private val cronExpression: CronExpression,
) {

    /** [after] 이후 첫 발화 시각. */
    fun next(after: LocalDateTime): LocalDateTime =
        checkNotNull(cronExpression.next(after)) {
            "다음 실행 시각이 없는 cron 입니다: $expression"
        }

    override fun toString(): String =
        expression

    companion object {

        /** @throws IllegalArgumentException 형식이 잘못됐거나 초 필드가 `0` 이 아닐 때 */
        fun parse(expression: String): BatchCron {
            val trimmed = expression.trim()
            val fields = trimmed.split(Regex("\\s+"))
            require(fields.size == 6) {
                "cron 은 초 분 시 일 월 요일 6개 필드여야 합니다: $expression"
            }
            require(fields[0] == "0") {
                "폴러가 1분마다 확인하므로 초 필드는 0 이어야 합니다: $expression"
            }
            return BatchCron(trimmed, CronExpression.parse(trimmed))
        }

    }

}
