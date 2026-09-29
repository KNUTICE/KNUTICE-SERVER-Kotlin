package com.fx.crawler.application.port.`in`

import com.fx.common.domain.TopicType

interface NoticeCrawlUseCase {

    /** [topicType] 게시판을 크롤링해 새 공지를 저장하고 저장한 수를 돌려준다. */
    fun crawlAndSave(topicType: TopicType): Int

}
