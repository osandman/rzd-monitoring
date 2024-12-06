# Пайплайн сборки проекта в докере
# 1. Этап сборки
FROM openjdk:17-alpine AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package

# 2. Финальный образ
FROM openjdk:17-alpine
LABEL "com.docker.compose.project"="osandman"
WORKDIR /app
COPY --from=builder /app/target/rzd-monitoring-0.0.1.jar ./rzd-monitoring.jar
ENTRYPOINT ["java", "-jar", "rzd-monitoring.jar"]