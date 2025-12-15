# Multi-stage build: compila con Maven y luego usa una imagen JRE liviana

# Etapa de build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# Etapa de runtime
FROM eclipse-temurin:17-jre
ARG APP_DIR=/app
WORKDIR ${APP_DIR}
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8761
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
