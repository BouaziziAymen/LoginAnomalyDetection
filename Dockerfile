# ---- Build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Gradle wrapper + build files first (better layer caching)
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle* settings.gradle* ./

# Then sources
COPY src ./src

# Build the runnable jar
RUN chmod +x gradlew && ./gradlew clean bootJar -x test

# ---- Run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Optional: run as non-root
RUN useradd -m appuser
USER appuser

# Copy the fat jar produced by Spring Boot
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

# Spring config comes from env vars (SPRING_*) or application.yml
ENTRYPOINT ["java","-jar","/app/app.jar"]
g