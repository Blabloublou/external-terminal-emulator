FROM gradle:8.5-jdk17

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY src src

CMD ["gradle", "run", "--console=plain", "--quiet"]
