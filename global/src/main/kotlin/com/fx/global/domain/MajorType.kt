package com.fx.global.domain

/**
 * 학과 유형을 나타내는 enum 클래스.
 * 각 학과는 rootDomain, bbsPath, category, college 정보를 포함
 * ENUM 에 포함된 학과는 서버에서 자동 크롤링 대상이 됨
 */
enum class MajorType(
    override val rootDomain: String,
    override val bbsPath: String,
    override val category: String,
    val college: String // 단과대 구분
) : CrawlableType {
    /**
     * 단과대 목록 (2026.04.15 기준)
     * - 공과대학 (14개)
     * - 교통공과대학 (3개)
     * - AI융합대학 (4개)
     * - 인문대학 (6개)
     * - 사회과학대학 (10개)
     * - 보건생명대학 (8개)
     * - 철도대학 (7개)
     * - 미래융합대학 (6개)
     */

    // dslee (2026.04.15) : 페이지 접속은 가능하나 2026년 단과대 및 학부 개편을 통해 사라진듯 함
//    AI_ROBOTICS_ENGINEERING(
//        "https://www.ut.ac.kr",
//        "/cop/bbs/BBSMSTR_000000001156/selectBoardList.do",
//        "AI로봇공학과",
//        "융합기술대학"
//    ),
//
//    BIOMEDICAL_ENGINEERING(
//        "https://www.ut.ac.kr",
//        "/cop/bbs/BBSMSTR_000000001139/selectBoardList.do",
//        "바이오메디컬융합학과",
//        "융합기술대학"
//    ),
//
//    PRECISION_MEDICINE_MEDICAL_DEVICE(
//        "https://www.ut.ac.kr",
//        "/cop/bbs/BBSMSTR_000000001382/selectBoardList.do",
//        "정밀의료·의료기기학과",
//        "융합기술대학"
//    ),


    // ---------- 공과대학 | College of Engineering ----------
    MECHANICAL_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000219/selectBoardList.do",
        "기계공학과",
        "ENGINEERING"
    ),

    ELECTRICAL_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000235/selectBoardList.do",
        "전기공학과",
        "ENGINEERING"
    ),

    ELECTRONIC_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000244/selectBoardList.do",
        "전자공학과",
        "ENGINEERING"
    ),

    CIVIL_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000259/selectBoardList.do",
        "사회기반공학전공",
        "ENGINEERING"
    ),

    ENVIRONMENTAL_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000264/selectBoardList.do",
        "환경공학전공",
        "ENGINEERING"
    ),

    URBAN_AND_TRANSPORTATION_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000269/selectBoardList.do",
        "도시·교통공학전공",
        "ENGINEERING"
    ),

    CHEMICAL_AND_BIOLOGICAL_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000277/selectBoardList.do",
        "화공생물공학과",
        "ENGINEERING"
    ),

    MATERIALS_SCIENCE_AND_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000281/selectBoardList.do",
        "반도체신소재공학과",
        "ENGINEERING"
    ),

    POLYMER_SCIENCE_AND_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000285/selectBoardList.do",
        "나노화학소재공학과",
        "ENGINEERING"
    ),

    SAFETY_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000304/selectBoardList.do",
        "안전공학과",
        "ENGINEERING"
    ),

    ARCHITECTURAL_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000319/selectBoardList.do",
        "건축공학과",
        "ENGINEERING"
    ),

    ARCHITECTURE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000328/selectBoardList.do",
        "건축학과(5년제)",
        "ENGINEERING"
    ),

    COMMUNICATION_DESIGN(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000342/selectBoardList.do",
        "커뮤니케이션디자인학과",
        "ENGINEERING"
    ),

    SCHOOL_OF_NANOMEDICAL_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000001501/selectBoardList.do",
        "나노메디컬공학부",
        "ENGINEERING"
    ),

    // ---------- 교통공과대학 | College of Transportation Engineering ----------
    AUTOMOTIVE_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000222/selectBoardList.do",
        "자동차공학과",
        "TRANSPORTATION_ENGINEERING"
    ),

    AERONAUTICAL_AND_MECHANICAL_DESIGN_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000226/selectBoardList.do",
        "항공·기계설계전공",
        "TRANSPORTATION_ENGINEERING"
    ),

    UNMANNED_AIRCRAFT_SYSTEM_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000001503/selectBoardList.do",
        "드론공학전공",
        "TRANSPORTATION_ENGINEERING"
    ),

    // ---------- AI융합대학 | College of AI Convergence ----------
    COMPUTER_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000352/selectBoardList.do",
        "컴퓨터공학과",
        "AI_CONVERGENCE",
    ),

    COMPUTER_SCIENCE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000360/selectBoardList.do",
        "컴퓨터소프트웨어학과",
        "AI_CONVERGENCE"
    ),

    INDUSTRIAL_AND_MANAGEMENT_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000301/selectBoardList.do",
        "산업경영공학과",
        "AI_CONVERGENCE"
    ),

    INDUSTRIAL_DESIGN(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000337/selectBoardList.do",
        "산업디자인학과",
        "AI_CONVERGENCE"
    ),

    // ---------- 인문대학 | College of Humanities ----------
    ENGLISH_LANGUAGE_AND_LITERATURE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000376/selectBoardList.do",
        "영어영문학과",
        "HUMANITIES"
    ),

    CHINESE_LANGUAGE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000385/selectBoardList.do",
        "중국어학과",
        "HUMANITIES"
    ),

    KOREAN_LANGUAGE_AND_LITERATURE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000390/selectBoardList.do",
        "한국어문학과",
        "HUMANITIES"
    ),

    MUSIC(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000463/selectBoardList.do",
        "음악학과",
        "HUMANITIES"
    ),

    SPORTS_MEDICINE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000472/selectBoardList.do",
        "스포츠의학과",
        "HUMANITIES"
    ),

    SPORTS_INDUSTRY(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000478/selectBoardList.do",
        "스포츠산업학과",
        "HUMANITIES"
    ),

    // ---------- 사회과학대학 | College of Social Sciences ----------
    PUBLIC_ADMINISTRATION(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000397/selectBoardList.do",
        "행정학과",
        "SOCIAL_SCIENCES"
    ),

    PUBLIC_ADMINISTRATION_AND_INFORMATION_CONVERGENCE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000402/selectBoardList.do",
        "행정정보융합학과",
        "SOCIAL_SCIENCES"
    ),

    BUSINESS_ADMINISTRATION(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000437/selectBoardList.do",
        "경영학과",
        "SOCIAL_SCIENCES"
    ),

    CONVERGENCE_MANAGEMENT(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000446/selectBoardList.do",
        "융합경영학과",
        "SOCIAL_SCIENCES"
    ),

    INTERNATIONAL_TRADE_AND_BUSINESS(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000451/selectBoardList.do",
        "국제무역학과",
        "SOCIAL_SCIENCES"
    ),

    SOCIAL_WELFARE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000458/selectBoardList.do",
        "사회복지학과",
        "SOCIAL_SCIENCES"
    ),

    AIRLINE_SERVICE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000483/selectBoardList.do",
        "항공서비스학과",
        "SOCIAL_SCIENCES"
    ),

    AERONAUTICAL_SCIENCE_AND_FLIGHT_OPERATION(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000487/selectBoardList.do",
        "항공운항학과",
        "SOCIAL_SCIENCES"
    ),

    EARLY_CHILDHOOD_EDUCATION(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000492/selectBoardList.do",
        "유아교육학과",
        "SOCIAL_SCIENCES"
    ),

    MEDIA_AND_CONTENTS(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000001147/selectBoardList.do",
        "미디어&콘텐츠학과",
        "SOCIAL_SCIENCES"
    ),

    // ---------- 보건생명대학 | College of Health and Life Science ----------
    NURSING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000592/selectBoardList.do",
        "간호학과",
        "HEALTH_AND_LIFE_SCIENCE"
    ),

    PHYSICAL_THERAPY(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000534/selectBoardList.do",
        "물리치료학과",
        "HEALTH_AND_LIFE_SCIENCE"
    ),

    PARAMEDICINE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000538/selectBoardList.do",
        "응급구조학과",
        "HEALTH_AND_LIFE_SCIENCE"
    ),

    FOOD_SCIENCE_AND_TECHNOLOGY(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000518/selectBoardList.do",
        "식품공학전공",
        "HEALTH_AND_LIFE_SCIENCE"
    ),

    FOOD_AND_NUTRITION(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000523/selectBoardList.do",
        "식품영양학전공",
        "HEALTH_AND_LIFE_SCIENCE"
    ),

    BIOTECHNOLOGY(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000528/selectBoardList.do",
        "생명공학전공",
        "HEALTH_AND_LIFE_SCIENCE"
    ),

    EARLY_CHILDHOOD_SPECIAL_EDUCATION(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000544/selectBoardList.do",
        "유아특수교육학과",
        "HEALTH_AND_LIFE_SCIENCE"
    ),

    IT_APPLIED_CONVERGENCE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000550/selectBoardList.do",
        "IT응용융합학과",
        "HEALTH_AND_LIFE_SCIENCE"
    ),

    // ---------- 철도대학 | College of Railroad Sciences ----------
    RAILROAD_MANAGEMENT_AND_LOGISTICS(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000557/selectBoardList.do",
        "철도경영·물류학과",
        "RAILROAD_SCIENCES"
    ),

    DATA_SCIENCE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000561/selectBoardList.do",
        "철도데이터사이언스전공",
        "RAILROAD_SCIENCES"
    ),

    ARTIFICIAL_INTELLIGENCE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000001163/selectBoardList.do",
        "철도인공지능전공",
        "RAILROAD_SCIENCES"
    ),

    RAILROAD_OPERATION_SYSTEMS_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000572/selectBoardList.do",
        "철도운전시스템공학과",
        "RAILROAD_SCIENCES"
    ),

    RAILWAY_VEHICLE_SYSTEM_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000576/selectBoardList.do",
        "철도차량시스템공학과",
        "RAILROAD_SCIENCES"
    ),

    RAILROAD_INFRASTRUCTURE_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000580/selectBoardList.do",
        "철도인프라공학과",
        "RAILROAD_SCIENCES"
    ),

    RAILROAD_ELECTRICAL_AND_INFORMATION_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000584/selectBoardList.do",
        "철도전기정보공학과",
        "RAILROAD_SCIENCES"
    ),

    // ---------- 미래융합대학 | College of Future Convergence ----------
    SAFETY_CONVERGENCE_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000001181/selectBoardList.do",
        "안전융합공학과",
        "FUTURE_CONVERGENCE"
    ),

    CONSTRUCTION_AND_DISASTER_PREVENTION_CONVERGENCE_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000001052/selectBoardList.do",
        "건설방재융합공학과",
        "FUTURE_CONVERGENCE"
    ),

    SPORT_WELFARE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000001053/selectBoardList.do",
        "스포츠복지학과",
        "FUTURE_CONVERGENCE"
    ),

    WELFARE_AND_MANAGEMENT(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000001054/selectBoardList.do",
        "복지·경영학과",
        "FUTURE_CONVERGENCE"
    ),

    SMART_RAILWAY_AND_TRANSPORTATION_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000001184/selectBoardList.do",
        "스마트철도교통공학과",
        "FUTURE_CONVERGENCE"
    ),

    SECONDARY_BATTERY_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000001351/selectBoardList.do",
        "이차전지공학과",
        "FUTURE_CONVERGENCE"
    )
    ;

    override val topicName: String
        get() = name

    init {
        CrawlableType.register(this)
    }

}