package com.fx.common.domain.i18n

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class LocalizedTextTest {

    @Test
    fun `요청 언어 값이 있으면 그 값을 쓴다`() {
        val text = LocalizedText(ko = "일반소식", en = "General News", ja = "一般ニュース")

        assertThat(text.resolve(Language.KO)).isEqualTo("일반소식")
        assertThat(text.resolve(Language.EN)).isEqualTo("General News")
        assertThat(text.resolve(Language.JA)).isEqualTo("一般ニュース")
    }

    @Test
    fun `요청 언어 값이 없으면 한국어로 대체한다`() {
        val text = LocalizedText(ko = "일반소식")

        assertThat(text.resolve(Language.EN)).isEqualTo("일반소식")
        assertThat(text.resolve(Language.JA)).isEqualTo("일반소식")
    }

    @Test
    fun `한국어는 비어 있을 수 없다`() {
        assertThatThrownBy {
            LocalizedText(ko = " ")
        }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `값이 같으면 같은 값 객체다`() {
        assertThat(LocalizedText("a", "b")).isEqualTo(LocalizedText("a", "b"))
        assertThat(LocalizedText("a", "b").copy()).isEqualTo(LocalizedText("a", "b"))
        assertThat(LocalizedText("a", "b")).isNotEqualTo(LocalizedText("a", "c"))
    }

}
