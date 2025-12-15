# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /workspace/app

# Copy Maven configuration
COPY ms-book/pom.xml ./
COPY ms-book/.mvn .mvn/
COPY ms-book/mvnw ./
RUN chmod +x mvnw

# Create Maven repository directories
RUN mkdir -p /root/.m2/repository/bo/edu/ucb/microservices/dto/dto-book/1.0-SNAPSHOT
RUN mkdir -p /root/.m2/repository/com/library/util/util/0.0.1-SNAPSHOT

# Copy dependencies from parent directory builds
COPY dto-book/target/dto-book-1.0-SNAPSHOT.jar /root/.m2/repository/bo/edu/ucb/microservices/dto/dto-book/1.0-SNAPSHOT/
COPY util/target/util-0.0.1-SNAPSHOT.jar /root/.m2/repository/com/library/util/util/0.0.1-SNAPSHOT/

# Copy POM files for dependencies
COPY dto-book/pom.xml /root/.m2/repository/bo/edu/ucb/microservices/dto/dto-book/1.0-SNAPSHOT/dto-book-1.0-SNAPSHOT.pom
COPY util/pom.xml /root/.m2/repository/com/library/util/util/0.0.1-SNAPSHOT/util-0.0.1-SNAPSHOT.pom

# Download remaining dependencies
RUN ./mvnw dependency:go-offline -DskipTests || true

# Copy source code and build
COPY ms-book/src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /workspace/app/target/*.jar app.jar

# Expose port
EXPOSE 8001

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8001/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]