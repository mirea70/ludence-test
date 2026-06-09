FROM postgres:17-alpine AS db

FROM gradle:8.14.3-jdk21-alpine AS builder
WORKDIR /workspace

COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine AS app
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p /data/images \
    && chown -R spring:spring /app /data

COPY --from=builder --chown=spring:spring /workspace/build/libs/*.jar app.jar

USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
