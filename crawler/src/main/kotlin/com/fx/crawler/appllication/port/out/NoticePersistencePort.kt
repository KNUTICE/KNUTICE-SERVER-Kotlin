package com.fx.crawler.appllication.port.out

import com.fx.crawler.domain.Notice

interface NoticePersistencePort {

    fun findByNttIdIn(nttIds: List<Long>): List<Notice>
    fun saveAll(notices: List<Notice>)

}