# Java 21 이미지를 기반으로 사용
FROM openjdk:21-jdk-slim

# 빌드된 애플리케이션 JAR 파일을 컨테이너에 복사
COPY build/libs/sparkle-note-0.0.1-SNAPSHOT.jar app.jar

# 포트 설정 (백엔드 애플리케이션이 사용할 포트)
EXPOSE 8080

# 애플리케이션 실행 명령어
ENTRYPOINT ["java", "-jar", \
            "-Duser.timezone=Asia/Seoul", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "/app.jar"]