FROM gradle:8-jdk21 AS build
WORKDIR /app
COPY gradle/libs.versions.toml gradle/libs.versions.toml
COPY build.gradle.kts settings.gradle.kts ./
RUN gradle dependencies --no-daemon
COPY src ./src
RUN gradle test --no-daemon
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/app.jar app.jar
COPY --from=build /app/build/reports/tests/test /app/test-reports
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
