package com.fx.common.domain.i18n

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource
import java.util.Locale

class LanguageTest {

    @ParameterizedTest
    @CsvSource("ko,KO", "en,EN", "ja,JA", "JA,JA", "ja-JP,JA", "ja_JP,JA", "en-US,EN", "' ko ',KO")
    fun `대소문자와 지역 코드를 무시하고 언어를 해석한다`(code: String, expected: Language) {
        assertThat(Language.from(code)).isEqualTo(expected)
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = ["zh", "fr-FR", "korean", "  ", "-"])
    fun `지원하지 않거나 알 수 없는 값은 한국어로 대체한다`(code: String?) {
        assertThat(Language.from(code)).isEqualTo(Language.KO)
    }

    @Test
    fun `Locale 로도 해석한다`() {
        assertThat(Language.from(Locale.JAPAN)).isEqualTo(Language.JA)
        assertThat(Language.from(Locale.CHINA)).isEqualTo(Language.KO)
        assertThat(Language.from(null as Locale?)).isEqualTo(Language.KO)
    }

}
