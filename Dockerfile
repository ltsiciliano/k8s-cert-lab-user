# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom and source
COPY pom.xml .
COPY src ./src

# Build the application (skip tests for faster image build)
RUN mvn -B -DskipTests clean package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-alpine

# Default port (can be overridden by -e PORT=...)
ENV PORT=7050
ENV JAVA_OPTS=""

# Create non-root user
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

# Copy the built jar
COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 7050
USER app

# Allow passing extra JVM options via JAVA_OPTS
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
