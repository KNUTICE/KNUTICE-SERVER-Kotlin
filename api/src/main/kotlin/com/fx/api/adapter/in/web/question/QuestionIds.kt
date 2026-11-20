package com.fx.api.adapter.`in`.web.question

import com.fx.api.exception.QuestionException
import com.fx.api.exception.errorcode.QuestionErrorCode

/** 질문 번호(TSID 문자열). 숫자가 아니면 없는 질문으로 다룬다 */
internal fun parseQuestionId(questionId: String): Long =
    questionId.toLongOrNull() ?: throw QuestionException(QuestionErrorCode.QUESTION_NOT_FOUND)
