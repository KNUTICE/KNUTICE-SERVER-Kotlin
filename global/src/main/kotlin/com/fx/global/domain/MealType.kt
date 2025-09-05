package com.fx.global.domain

enum class MealType(
    override val rootDomain: String,
    override val bbsPath: String,
    override val category: String
) : CrawlableType {

    STUDENT_CAFETERIA(
        "https://www.ut.ac.kr",
        "/prog/mealManage/MT01/kor/sub06_02_02_01/dayList.do",
        "학생식당"
    ),

    STAFF_CAFETERIA(
        "https://www.ut.ac.kr",
        "/prog/mealManage/MT02/kor/sub06_02_02_02/dayList.do",
        "교직원식당"
    );

    override val typeName: String
        get() = name

    init {
        CrawlableType.register(this)
    }

}