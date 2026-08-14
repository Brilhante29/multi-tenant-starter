FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts gradle.lockfile ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet
COPY src ./src
RUN ./gradlew test bootJar --no-daemon

ARG SOURCE_COMMIT=unknown
ARG DEPENDENCY_LOCK_DIGEST=unknown

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/app.jar app.jar
COPY --from=build /app/build/reports/tests/test /app/test-reports
ARG SOURCE_COMMIT=unknown
ARG DEPENDENCY_LOCK_DIGEST=unknown
ENV APP_SOURCE_COMMIT=${SOURCE_COMMIT}
ENV APP_DEPENDENCY_LOCK_DIGEST=${DEPENDENCY_LOCK_DIGEST}
LABEL org.opencontainers.image.source="https://github.com/Brilhante29/multi-tenant-starter"
LABEL org.opencontainers.image.revision=${SOURCE_COMMIT}
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
