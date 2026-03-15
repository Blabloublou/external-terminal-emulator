FROM gradle:8.5-jdk17

WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
RUN gradle dependencies --quiet

COPY src src
RUN gradle installDist --no-daemon --quiet

CMD ["/app/build/install/terminal-buffer/bin/terminal-buffer"]
