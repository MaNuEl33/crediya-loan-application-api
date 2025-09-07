# -------- STAGE 1: Build --------
FROM gradle:8.10.2-jdk17-alpine AS builder

WORKDIR /builder

# Copy configuration files first (better dependency caching)
COPY build.gradle settings.gradle gradlew gradle.properties main.gradle ./
COPY gradle ./gradle

# Download cached dependencies
RUN ./gradlew dependencies --no-daemon || return 0

# Copy the rest of the project and compile
COPY . .
RUN ./gradlew clean build --no-daemon

# -------- STAGE 2: Runtime --------
FROM eclipse-temurin:17-jdk-alpine

# Create non user root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy only the JAR generated from the build stage
COPY --from=builder /builder/applications/app-service/build/libs/*.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"

USER appuser

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]