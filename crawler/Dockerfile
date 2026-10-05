FROM gradle:9.8.0-jdk25 AS build
WORKDIR /app
COPY . .
RUN gradle :crawler:bootJar --no-daemon

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/crawler/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]