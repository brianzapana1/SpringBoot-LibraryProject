@echo off
echo Iniciando MS-Book con perfil 'dev' y registro en Eureka...
echo.
echo IMPORTANTE: Asegurate de que Eureka Server esté corriendo en puerto 8761
echo.
echo Servicios disponibles después del inicio:
echo - MS-Book API: http://localhost:8082/books
echo - Discovery Endpoints: http://localhost:8082/discovery/services
echo - Eureka Dashboard: http://localhost:8761
echo.
cd /d "%~dp0ms-book"
set SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run
pause