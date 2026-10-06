# jar 는 CI 러너에서 Gradle 로 미리 만든다 (./gradlew build). 이미지는 실행만 한다
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY crawler/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
