# Stage 1: Build the application
FROM openjdk:17-jdk-slim as build

# Install git (needed for config server git repository access)
RUN apt-get update && apt-get install -y git && apt-get clean

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw pom.xml ./
COPY .mvn .mvn/

# Download dependencies
RUN ./mvnw dependency:resolve

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:17-jre-slim

WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/gateway-*.jar app.jar

# Expose the port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]