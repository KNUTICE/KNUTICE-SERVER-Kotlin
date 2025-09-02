package com.fx.global.domain

enum class MajorType(
    override val rootDomain: String,
    override val bbsPath: String,
    override val category: String
) : CrawlableType {

    // ---------- 융합기술대학 ----------
    MECHANICAL_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000219/selectBoardList.do",
        "기계공학과"
    ),

    AUTOMOTIVE_ENGINEERING(
    "https://www.ut.ac.kr",
    "/cop/bbs/BBSMSTR_000000000222/selectBoardList.do",
    "자동차공학과"
    ),

    AERONAUTICAL_AND_MECHANICAL_DESIGN_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000226/selectBoardList.do",
        "항공·기계설계학과"
    ),

    ELECTRICAL_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000235/selectBoardList.do",
        "전기공학과"
    ),

    ELECTRONIC_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000244/selectBoardList.do",
        "전자공학과"
    ),

    COMPUTER_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000352/selectBoardList.do",
        "컴퓨터공학과"
    ),

    COMPUTER_SCIENCE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000360/selectBoardList.do",
        "컴퓨터소프트웨어학과"
    ),

    AI_ROBOTICS_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000001156/selectBoardList.do",
        "AI로봇공학과"
    ),

    BIOMEDICAL_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000001139/selectBoardList.do",
        "바이오메디컬융합학과"
    ),

    PRECISION_MEDICINE_MEDICAL_DEVICE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000001382/selectBoardList.do",
        "정밀의료·의료기기학과"
    ),


    // ---------- 공과대학 ----------
    CIVIL_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000259/selectBoardList.do",
        "사회기반공학전공"
    ),

    ENVIRONMENTAL_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000264/selectBoardList.do",
        "환경공학전공"
    ),

    URBAN_AND_TRANSPORTATION_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000269/selectBoardList.do",
        "도시·교통공학전공"
    ),

    CHEMICAL_AND_BIOLOGICAL_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000277/selectBoardList.do",
        "화공생물공학과"
    ),

    MATERIALS_SCIENCE_AND_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000281/selectBoardList.do",
        "반도체신소재공학과"
    ),

    POLYMER_SCIENCE_AND_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000285/selectBoardList.do",
        "나노화학소재공학과"
    ),

    INDUSTRIAL_AND_MANAGEMENT_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000301/selectBoardList.do",
        "산업경영공학과"
    ),

    SAFETY_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000304/selectBoardList.do",
        "안전공학과"
    ),

    ARCHITECTURAL_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000319/selectBoardList.do",
        "건축공학과"
    ),

    ARCHITECTURE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000328/selectBoardList.do",
        "건축학과(5년제)"
    ),

    INDUSTRIAL_DESIGN(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000337/selectBoardList.do",
        "산업디자인학과"
    ),

    COMMUNICATION_DESIGN(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000342/selectBoardList.do",
        "커뮤니케이션디자인학과"
    ),

    // ---------- 인문대학 ----------
    ENGLISH_LANGUAGE_AND_LITERATURE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000376/selectBoardList.do",
        "영어영문학과"
    ),

    CHINESE_LANGUAGE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000385/selectBoardList.do",
        "중국어학과"
    ),

    KOREAN_LANGUAGE_AND_LITERATURE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000390/selectBoardList.do",
        "한국어문학과"
    ),

    MUSIC(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000463/selectBoardList.do",
        "음악학과"
    ),

    SPORTS_MEDICINE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000472/selectBoardList.do",
        "스포츠의학과"
    ),

    SPORTS_INDUSTRY(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000478/selectBoardList.do",
        "스포츠산업학과"
    ),

    // ---------- 사회과학대학 ----------
    PUBLIC_ADMINISTRATION(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000397/selectBoardList.do",
        "행정학과"
    ),

    PUBLIC_ADMINISTRATION_AND_INFORMATION_CONVERGENCE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000402/selectBoardList.do",
        "행정정보융합학과"
    ),

    BUSINESS_ADMINISTRATION(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000437/selectBoardList.do",
        "경영학과"
    ),

    CONVERGENCE_MANAGEMENT(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000446/selectBoardList.do",
        "융합경영학과"
    ),

    INTERNATIONAL_TRADE_AND_BUSINESS(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000451/selectBoardList.do",
        "국제무역학과"
    ),

    SOCIAL_WELFARE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000458/selectBoardList.do",
        "사회복지학과"
    ),

    AIRLINE_SERVICE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000483/selectBoardList.do",
        "항공서비스학과"
    ),

    AERONAUTICAL_SCIENCE_AND_FLIGHT_OPERATION(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000487/selectBoardList.do",
        "항공운항학과"
    ),

    EARLY_CHILDHOOD_EDUCATION(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000492/selectBoardList.do",
        "유아교육학과"
    ),

    MEDIA_AND_CONTENTS(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000001147/selectBoardList.do",
        "미디어&콘텐츠학과"
    ),

    // ---------- 보건생명대학 ----------
    NURSING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000592/selectBoardList.do",
        "간호학과"
    ),

    PHYSICAL_THERAPY(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000534/selectBoardList.do",
        "물리치료학과"
    ),

    PARAMEDICINE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000538/selectBoardList.do",
        "응급구조학과"
    ),

    FOOD_SCIENCE_AND_TECHNOLOGY(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000518/selectBoardList.do",
        "식품공학전공"
    ),

    FOOD_AND_NUTRITION(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000523/selectBoardList.do",
        "식품영양학전공"
    ),

    BIOTECHNOLOGY(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000528/selectBoardList.do",
        "생명공학전공"
    ),

    EARLY_CHILDHOOD_SPECIAL_EDUCATION(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000544/selectBoardList.do",
        "유아특수교육학과"
    ),

    // ---------- 철도대학 ----------
    RAILROAD_MANAGEMENT_AND_LOGISTICS(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000557/selectBoardList.do",
        "철도경영·물류학과"
    ),

    DATA_SCIENCE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000561/selectBoardList.do",
        "데이터사이언스전공"
    ),

    ARTIFICIAL_INTELLIGENCE(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000001163/selectBoardList.do",
        "인공지능전공"
    ),

    RAILROAD_OPERATION_SYSTEMS_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000572/selectBoardList.do",
        "철도운전시스템공학과"
    ),

    RAILWAY_VEHICLE_SYSTEM_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000576/selectBoardList.do",
        "철도차량시스템공학과"
    ),

    RAILROAD_INFRASTRUCTURE_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000580/selectBoardList.do",
        "철도인프라공학과"
    ),

    RAILROAD_ELECTRICAL_AND_INFORMATION_ENGINEERING(
        "https://www.ut.ac.kr",
        "/cop/bbs/BBSMSTR_000000000584/selectBoardList.do",
        "철도전기정보공학과"
    ),
    ;

    override val typeName: String
        get() = name

    init {
        CrawlableType.register(this)
    }

}