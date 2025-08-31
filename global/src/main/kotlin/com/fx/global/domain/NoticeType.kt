package com.fx.global.domain

enum class NoticeType(
    override val rootDomain: String,
    override val bbsPath: String,
    override val category: String
) : CrawlableType {


    GENERAL_NEWS(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000059/selectBoardList.do",
        "일반소식"
    ),

    SCHOLARSHIP_NEWS(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000060/selectBoardList.do",
        "장학안내"
    ),

    EVENT_NEWS(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000061/selectBoardList.do",
        "행사안내"
    ),

    ACADEMIC_NEWS(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000055/selectBoardList.do",
        "학사공지사항"
    ),

    EMPLOYMENT_NEWS(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000179/selectBoardList.do",
        "취업지원안내"
    );

    override val typeName: String
        get() = name

    init {
        CrawlableType.register(this)
    }

}