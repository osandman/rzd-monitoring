# Пайплайн сборки проекта в докере
# https://docs.docker.com/get-started/docker-concepts/building-images/multi-stage-builds/
# 1. Этап сборки
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml .env ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw clean package

# 2. Финальный образ
FROM eclipse-temurin:17-jre-jammy
LABEL "com.docker.compose.project"="osandman"
WORKDIR /app
EXPOSE 8088
COPY --from=builder /app/target/rzd-monitoring-0.0.1.jar ./rzd-monitoring.jar
COPY .env .env
ENTRYPOINT ["java", "-jar", "rzd-monitoring.jar"]