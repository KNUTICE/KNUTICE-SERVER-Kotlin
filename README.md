<p align="middle" >
  <img width="1920" height="1180" alt="readme-main" src="https://github.com/user-attachments/assets/f2d6f7e7-c193-4244-ab9a-d2a32f9db999"/>
</p>

<div align="center">
    <a href="https://apps.apple.com/kr/app/knutice/id6547855991">
        <img src="https://img.shields.io/badge/Apple Store-0D96F6?style=flat&logo=apple&logoColor=black">
    </a>
    <a href="https://play.google.com/store/apps/details?id=com.doyoonkim.knutice&hl=ko">
        <img src="https://img.shields.io/badge/Google Play-90c8ff?style=flat&logo=Google&logoColor=white">
    </a>
</div>

# 프로젝트 소개 🎓
학생들에게 꼭 필요한 공지사항을 놓치지 않도록 <br>
최신 기술을 통해 실시간으로 제공하며 <br>
효율적이고 스마트한 캠퍼스 라이프를 지원합니다. <br>
지금, KNUTICE와 함께 편리한 학교생활을 경험해보세요! <br>


# 사용 기술 ⚙️
| 구분 | 기술 스택 |
|------|-----------|
| 🖥️ Backend | Kotlin, Java, Spring Boot, QueryDSL, Firebase Cloud Messaging |
| 🗄️ Database | MongoDB |
| ☁️ Infra & DevOps | Docker, Docker Compose, Docker Multi-Stage Build, AWS Lightsail, Doppler, GitHub Actions |
| 🤝 Collaboration | Confluence, Slack |
| ⚡ 기타 | Kotlin Coroutine, Hexagonal Architecture |


# 리팩토링 🔧

## 📦 Layered Architecture ➝ Hexagonal Architecture
<details>
<summary>자세히 보기</summary>
  
기존 크롤링 로직은 체계적인 아키텍처를 갖추지 못해 유지보수성과 확장성이 부족했습니다.
특히 패키지 네이밍의 일관성이 떨어지고, 외부 모듈 교체/확장이 어려운 구조였습니다.

이를 개선하기 위해 Hexagonal Architecture로 리팩토링을 진행했습니다.

### 리팩토링 전 문제점
- **강한 결합도** : Service, Repository, 외부 API 호출 로직이 서로 직접적으로 의존  
- **패키지 구조 혼란** : 도메인/애플리케이션/인프라 구분이 모호하여 유지보수 시 혼란 발생

### 리팩토링 후 장점
- **관심사 분리**  
  - 도메인 로직과 인프라(크롤링, DB, Webhook 등) 의존성을 완전히 분리  
  - 핵심 비즈니스 로직이 외부 구현체와 독립적으로 동작
 
- **유지보수성 향상**  
  - 명확한 패키지 구조 (domain, application, adapter, port)  
  - 새로운 기능 추가 시 기존 코드 변경 최소화 (OCP 원칙 준수)

</details>


## ⚡ Java ➝ Kotlin Coroutine 도입
<details>
<summary>자세히 보기</summary>

우리 크롤링 서버는 크롤링 작업 특성상 **외부 IO 요청**이 빈번합니다.  
기존 Java 기반의 동기 처리 방식에서는 이러한 요청마다 스레드가 블로킹되어 **자원 활용도가 떨어지고, 처리량이 제한**되었습니다.  

이를 해결하기 위해 **Kotlin Coroutine**을 도입했습니다.  

### 도입 후
- **비동기/논블로킹 처리**  
  - Coroutine 기반으로 IO 요청을 처리하여, 스레드가 블로킹되지 않고 다른 작업을 이어갈 수 있음  
- **경량성 (Lightweight)**  
  - 수천 개의 Coroutine을 하나의 스레드 풀에서 효율적으로 실행 가능
  
</details>

## 🔑 Doppler 도입
<details>
<summary>자세히 보기</summary>

개발자 간 환경 변수를 공유할 때, 기존에는 **구두/메신저**를 통해 전달하거나  
별도의 문서에 정리하는 방식으로 진행했습니다.  

이러한 문제를 해결하기 위해 환경 변수 관리 도구인 **Doppler**를 도입했습니다.  
Doppler를 통해 환경 변수를 중앙에서 통합적으로 관리할 수 있게 되었고,  
모든 개발자와 서버가 동일한 환경을 자동으로 동기화할 수 있었습니다.  

</details>

## 🐳 Docker Multi-Stage Build
<details>
<summary>자세히 보기</summary>

초기 빌드 과정에서는 애플리케이션을 컴파일하고 실행하는 데 불필요한 라이브러리와 빌드 도구들이  
최종 이미지에 그대로 포함되었습니다. 이로 인해 이미지 용량이 불필요하게 커지는 경우가 있었습니다.

이를 개선하기 위해 **Docker Multi-Stage Build** 방식을 도입했습니다.  
멀티 스테이지 빌드를 통해 빌드 단계에서는 필요한 도구와 라이브러리만 포함시키고,  
최종 실행 단계에서는 애플리케이션 실행에 꼭 필요한 최소한의 파일만 남겨  
경량화된 이미지를 생성할 수 있었습니다.  

</details>
