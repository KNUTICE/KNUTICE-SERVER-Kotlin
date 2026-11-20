package com.fx.api.config.question

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 공지 질문(RAG) 설정 (`notice-question.*`).
 * API 키는 toString 으로 새지 않도록 여기 두지 않고 어댑터가 `notice-question.api-key` 를 직접 읽는다.
 *
 * @property baseUrl Gemini 기본 API 주소
 * @property embeddingModel 질문 임베딩 모델. 공지 청크를 만든 모델(crawler `crawler.embedding.model`)과 같아야 비교된다
 * @property answerModel 답변 모델
 * @property answerMaxOutputTokens 답변 최대 출력 토큰
 * @property dailyLimit 하루 질문 상한 (관리자 전체). 무료 한도를 crawler 의 요약 · 임베딩 배치와 나눠 쓰므로 작게 둔다
 * @property resetHour 하루 한도가 초기화되는 한국시간 (Gemini 무료 한도 초기화 시각)
 * @property decayPerYear 기본 검색 설정: 공지가 올라온 지 1년마다 빼는 점수
 * @property titlePenalty 기본 검색 설정: 제목만 있는 청크에서 빼는 점수
 * @property minSimilarity 근거로 쓸 최소 코사인 유사도. 이보다 낮으면 답변을 만들지 않는다
 * @property topNotices 근거 공지 수 기본값
 * @property chunksPerNotice 근거 공지마다 답변 모델에 넘기는 최대 청크 수
 * @property candidateChunks 점수 순으로 먼저 읽는 청크 수 (공지 단위로 묶기 전)
 * @property retrievedLimit 관리자 화면에 돌려주는 검색 결과 청크 수
 */
@ConfigurationProperties("notice-question")
data class NoticeQuestionProperties(
    val baseUrl: String = "https://generativelanguage.googleapis.com/v1beta",
    val embeddingModel: String = "gemini-embedding-2",
    val answerModel: String = "gemini-3.5-flash-lite",
    val answerMaxOutputTokens: Int = 700,
    val dailyLimit: Int = 50,
    val resetHour: Int = 17,
    val decayPerYear: Double = 0.04,
    val titlePenalty: Double = 0.0,
    val minSimilarity: Double = 0.60,
    val topNotices: Int = 5,
    val chunksPerNotice: Int = 3,
    val candidateChunks: Int = 150,
    val retrievedLimit: Int = 15,
) {

    init {
        require(resetHour in 0..23) {
            "notice-question.reset-hour 는 0 ~ 23 이어야 합니다: $resetHour"
        }
        require(dailyLimit > 0 && topNotices > 0 && chunksPerNotice > 0 && candidateChunks > 0) {
            "notice-question 의 개수 설정은 1 이상이어야 합니다."
        }
    }

}
