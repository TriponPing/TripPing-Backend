# 👈 새로 추가: Railway/Render 등에 배포할 때 "Java 21 + Gradle 9.5.1"을 호스팅 쪽이
# 알아서 추측하게 두지 않고, 여기서 정확한 버전으로 직접 빌드/실행 환경을 고정함.
# (buildpack 자동 감지에 맡기면 버전 불일치로 빌드가 실패할 수 있어서, 배포 성공률을 높이려고 추가.)

# ---- 1단계: 빌드 ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# gradle wrapper와 설정 파일만 먼저 복사해서 의존성 캐시가 최대한 재사용되게 함
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || true

# 나머지 소스 복사 후 실제 빌드 (테스트는 배포 속도를 위해 생략)
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# ---- 2단계: 실행 (가벼운 JRE 이미지) ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# 호스팅이 지정해주는 PORT 환경변수를 그대로 씀 (application.yml의 server.port: ${PORT:8080}과 짝)
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
