FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /workspace/app

COPY discovery-server/pom.xml ./
RUN mvn -B dependency:go-offline

COPY discovery-server/src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=builder /workspace/app/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]