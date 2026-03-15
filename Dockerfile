FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
RUN gradle dependencies --quiet

COPY src src
RUN gradle installDist --no-daemon --quiet

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=builder /app/build/install/terminal-buffer /app/app

CMD ["/app/app/bin/terminal-buffer"]
