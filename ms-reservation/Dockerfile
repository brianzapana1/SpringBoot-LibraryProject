# Multi-stage build for ms-reservation (build from repo ROOT using -f ms-reservation/Dockerfile)

# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 1) Build dto-reservation and install to local repo
COPY dto-reservation/pom.xml dto-reservation/pom.xml
COPY dto-reservation/src dto-reservation/src
RUN mvn -q -DskipTests -f dto-reservation/pom.xml install

# 2) Build util and install to local repo
COPY util/pom.xml util/pom.xml
COPY util/src util/src
RUN mvn -q -DskipTests -f util/pom.xml install

# 3) Build ms-reservation (now dependencies exist in local .m2)
COPY ms-reservation/pom.xml ms-reservation/pom.xml
COPY ms-reservation/src ms-reservation/src
RUN mvn -q -DskipTests -f ms-reservation/pom.xml package

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/ms-reservation/target/*.jar app.jar
EXPOSE 8084
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
