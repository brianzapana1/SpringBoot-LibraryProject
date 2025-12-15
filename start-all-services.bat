@echo off
echo ========================================
echo      INICIANDO SERVICIOS DE LA BIBLIOTECA
echo ========================================

echo.
echo [1/4] Iniciando Config Server...
cd config-server
start "Config Server" cmd /c "mvn spring-boot:run"
echo Config Server iniciando en puerto 8888...
cd ..

echo.
echo [2/4] Esperando Config Server... (10 segundos)
timeout /t 10 /nobreak > nul

echo.
echo [3/4] Iniciando Discovery Server...
cd discovery-server
start "Discovery Server" cmd /c "mvn spring-boot:run"
echo Discovery Server iniciando en puerto 8000...
cd ..

echo.
echo [4/4] Esperando Discovery Server... (15 segundos)
timeout /t 15 /nobreak > nul

echo.
echo [5/6] Iniciando MS-Book...
cd ms-book
start "MS-Book" cmd /c "mvn spring-boot:run"
echo MS-Book iniciando en puerto 8001...
cd ..

echo.
echo [6/6] Esperando MS-Book... (10 segundos)
timeout /t 10 /nobreak > nul

echo.
echo [7/7] Iniciando Gateway...
cd gateway
start "Gateway" cmd /c "mvn spring-boot:run"
echo Gateway iniciando en puerto 8080...
cd ..

echo.
echo ========================================
echo           SERVICIOS INICIADOS
echo ========================================
echo.
echo Config Server:    http://localhost:8888
echo Discovery Server: http://localhost:8000
echo MS-Book:          http://localhost:8001
echo Gateway:          http://localhost:8080
echo.
echo Gateway APIs:
echo - Libros:         http://localhost:8080/ms-book/v1/api/books
echo - Swagger UI:     http://localhost:8080/openapi/swagger-ui.html
echo - Health Check:   http://localhost:8080/actuator/health
echo.
echo ========================================

pause