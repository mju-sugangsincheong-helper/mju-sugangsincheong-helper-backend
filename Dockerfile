# Stage 1: Build
FROM gradle:8.11.1-jdk21 AS build
WORKDIR /home/gradle/project

# Copy only the files needed for dependency resolution to leverage Docker cache
COPY build.gradle settings.gradle gradlew /home/gradle/project/
COPY gradle /home/gradle/project/gradle

# Download dependencies
RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# Copy the rest of the source code
COPY . /home/gradle/project/

# Build the application
RUN ./gradlew --no-daemon clean build -x test

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install curl for health checks (optional)
RUN apk add --no-cache curl

# Copy the built jar from the build stage
# Assuming the jar name follows the default pattern
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

# Set default profile to prod
ENV SPRING_PROFILES_ACTIVE=prod

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
