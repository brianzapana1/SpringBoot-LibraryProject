FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copiar Maven Wrapper
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn ./.mvn

# Copiar POM
COPY pom.xml .

# Copiar código fuente
COPY src ./src

# Dar permisos y compilar
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests clean package

# ----------- IMAGEN FINAL -------------
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]