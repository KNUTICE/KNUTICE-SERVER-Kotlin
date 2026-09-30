package com.fx.migration

/** 컬렉션 하나의 이관 결과. 원본 건수 = 옮긴 건수 + 건너뛴 건수여야 한다. */
class CollectionResult(
    val name: String,
    val source: Long,
) {

    var migrated: Long = 0
        private set

    /** 건너뛴 사유 → 건수 */
    val skipped: MutableMap<String, Int> = linkedMapOf()

    /** 값을 고쳐서 옮긴 내역 → 건수 */
    val notes: MutableMap<String, Int> = linkedMapOf()

    fun migrated(count: Int) {
        migrated += count
    }

    fun skip(reason: String) {
        skipped.merge(reason, 1, Int::plus)
    }

    fun note(note: String) {
        notes.merge(note, 1, Int::plus)
    }

    val consistent: Boolean
        get() = source == migrated + skipped.values.sum()

}

/** 이관이 끝난 뒤 테이블 건수 확인. */
data class TableCheck(val table: String, val expected: Long, val actual: Long) {
    val matched: Boolean get() = expected == actual
}

class MigrationReport {

    val collections: MutableList<CollectionResult> = mutableListOf()
    val tableChecks: MutableList<TableCheck> = mutableListOf()

    /** 이관하지 않은 컬렉션 → 건수 (`mongodump` 백업 대상) */
    val excluded: MutableMap<String, Long> = linkedMapOf()

    val successful: Boolean
        get() = collections.all {
            it.consistent
        } && tableChecks.all {
            it.matched
        }

    fun format(): String =
        buildString {
            appendLine("==================== MongoDB → MySQL 이관 결과 ====================")
            collections.forEach { result ->
                appendLine("[${result.name}] 원본 ${result.source} = 이관 ${result.migrated} + 건너뜀 ${result.skipped.values.sum()}" +
                        if (result.consistent) "" else "  ← 건수 불일치")
                result.skipped.forEach { (reason, count) ->
                    appendLine("    건너뜀 · $reason : $count")
                }
                result.notes.forEach { (note, count) ->
                    appendLine("    고침 · $note : $count")
                }
            }
            appendLine("---- 테이블 건수 ----")
            tableChecks.forEach {
                appendLine("${it.table} : 기대 ${it.expected} / 실제 ${it.actual}" + if (it.matched) "" else "  ← 불일치")
            }
            appendLine("---- 이관하지 않은 컬렉션 ----")
            excluded.forEach { (name, count) ->
                appendLine("$name : $count")
            }
            append(if (successful) "결과 : 성공" else "결과 : 실패 (위 불일치 확인)")
        }

}
