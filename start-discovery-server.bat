@echo off
echo Iniciando Eureka Discovery Server...
echo.
echo Dashboard estará disponible en: http://localhost:8761
echo.
cd /d "%~dp0discovery-server\target"
java -jar discovery-server-0.0.1-SNAPSHOT.jar
pause