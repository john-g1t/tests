FROM gradle:9.2-jdk21 AS builder
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts ./
COPY --chown=gradle:gradle gradle ./gradle
RUN gradle dependencies --no-daemon
COPY --chown=gradle:gradle src ./src
RUN gradle installDist --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /home/gradle/src/build/install/api /app/

ENTRYPOINT ["/app/bin/api"]
