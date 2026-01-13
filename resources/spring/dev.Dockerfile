# =========================
# 1. Build stage
# =========================
FROM gradle:8.6-jdk21 AS builder

WORKDIR /prod-stack

COPY gradle gradle
COPY gradlew .
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

COPY src src

RUN ./gradlew bootJar --no-daemon


# =========================
# 2. Runtime stage
# =========================
FROM eclipse-temurin:21-jre

WORKDIR /prod-stack

COPY --from=builder /prod-stack/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]