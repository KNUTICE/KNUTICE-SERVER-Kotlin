package com.fx.common.domain.i18n

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

/**
 * 언어별 문자열 값 객체. 한국어는 필수이고, 영어 · 일본어가 없으면 한국어로 대체한다.
 *
 * 컬럼 이름 · 길이는 사용하는 엔티티에서 `@AttributeOverride` 로 지정한다. (예: `display_name_ko`)
 * 값을 바꿀 때는 새 인스턴스로 통째로 교체한다.
 */
@Embeddable
class LocalizedText(
    ko: String,
    en: String? = null,
    ja: String? = null,
) {

    @Column(name = "ko", nullable = false)
    var ko: String = ko
        protected set

    @Column(name = "en", nullable = true)
    var en: String? = en
        protected set

    @Column(name = "ja", nullable = true)
    var ja: String? = ja
        protected set

    init {
        require(ko.isNotBlank()) { "한국어 문구는 비어 있을 수 없습니다." }
    }

    fun resolve(language: Language): String =
        when (language) {
            Language.KO -> ko
            Language.EN -> en ?: ko
            Language.JA -> ja ?: ko
        }

    /** 언어별 값을 모두 담은 목록. 값이 없는 언어는 뺀다. */
    fun values(): Map<Language, String> =
        buildMap {
            put(Language.KO, ko)
            en?.let { put(Language.EN, it) }
            ja?.let { put(Language.JA, it) }
        }

    fun copy(): LocalizedText = LocalizedText(ko, en, ja)

    override fun equals(other: Any?): Boolean =
        this === other || (other is LocalizedText && ko == other.ko && en == other.en && ja == other.ja)

    override fun hashCode(): Int = listOf(ko, en, ja).hashCode()

    override fun toString(): String = "LocalizedText(ko=$ko, en=$en, ja=$ja)"

}
