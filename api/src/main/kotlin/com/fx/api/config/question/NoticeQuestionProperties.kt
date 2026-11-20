package com.fx.api.config.question

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * 공지 질문(RAG) 설정 (`notice-question.*`).
 *
 * 답변은 서버가 만들지 않는다. 서버 키(`notice-question.api-key`)는 질문 임베딩에만 쓰고,
 * 답변은 앱 · 관리자 웹이 사용자 개인 키(팩트챗 · Gemini)로 직접 받는다.
 * 키 · 서명 비밀값은 toString 으로 새지 않도록 여기 두지 않고 쓰는 곳에서 직접 읽는다.
 *
 * @property baseUrl Gemini 기본 API 주소 (질문 임베딩)
 * @property embeddingModel 질문 임베딩 모델. 공지 청크를 만든 모델(crawler `crawler.embedding.model`)과 같아야 비교된다
 * @property appDailyLimit 앱 기기(fcmToken)마다 하루 질문 수. 근거를 못 찾은 질문은 세지 않는다
 * @property adminDailyLimit 관리자 전체 하루 질문 수
 * @property totalDailyLimit 서버 전체 하루 질문 수. 질문마다 서버 무료 키로 임베딩 1회를 쓰므로,
 *   crawler 의 요약 · 임베딩 배치와 나눠 쓰는 무료 한도를 넘지 않게 막는다
 * @property resetHour 하루 한도가 초기화되는 한국시간
 * @property finishTokenTtl 프롬프트를 받은 뒤 답변을 돌려줘야 하는 시간
 * @property answerMaxLength 저장하는 답변 최대 길이 (넘으면 자른다)
 * @property answerTemperature · answerMaxOutputTokens 앱 · 웹이 AI 를 부를 때 쓰라고 함께 내려 주는 값
 * @property decayPerYear 기본 검색 설정: 공지가 올라온 지 1년마다 빼는 점수
 * @property titlePenalty 기본 검색 설정: 제목만 있는 청크에서 빼는 점수
 * @property minSimilarity 근거로 쓸 최소 코사인 유사도. 이보다 낮으면 NOT_FOUND (AI 를 부를 필요 없음)
 * @property topNotices 근거 공지 수 기본값
 * @property chunksPerNotice 근거 공지마다 프롬프트에 넣는 최대 청크 수
 * @property candidateChunks 점수 순으로 먼저 읽는 청크 수 (공지 단위로 묶기 전)
 * @property retrievedLimit 관리자 화면에 돌려주는 검색 결과 청크 수
 */
@ConfigurationProperties("notice-question")
data class NoticeQuestionProperties(
    val baseUrl: String = "https://generativelanguage.googleapis.com/v1beta",
    val embeddingModel: String = "gemini-embedding-2",
    val appDailyLimit: Int = 3,
    val adminDailyLimit: Int = 50,
    val totalDailyLimit: Int = 300,
    val resetHour: Int = 0,
    val finishTokenTtl: Duration = Duration.ofMinutes(10),
    val answerMaxLength: Int = 2000,
    val answerTemperature: Double = 0.2,
    val answerMaxOutputTokens: Int = 700,
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
        require(appDailyLimit > 0 && adminDailyLimit > 0 && totalDailyLimit > 0) {
            "notice-question 의 하루 한도는 1 이상이어야 합니다."
        }
        require(topNotices > 0 && chunksPerNotice > 0 && candidateChunks > 0 && answerMaxLength > 0) {
            "notice-question 의 개수 설정은 1 이상이어야 합니다."
        }
    }

}
