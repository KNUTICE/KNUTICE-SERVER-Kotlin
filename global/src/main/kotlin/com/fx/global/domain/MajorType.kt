package com.fx.global.domain

enum class MajorType(
    override val rootDomain: String,
    override val bbsPath: String,
    override val category: String
) : CrawlableType {

    COMPUTER_SCIENCE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000360/selectBoardList.do",
        "컴퓨터소프트웨어학과"
    );

    override val typeName: String
        get() = name

    init {
        CrawlableType.register(this)
    }

}