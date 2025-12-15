@echo off
echo ========================================
echo    CONSTRUYENDO IMAGENES DOCKER
echo ========================================

echo.
echo [1/4] Construyendo Config Server...
cd config-server
docker build -t library/config-server:latest .
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo al construir config-server
    exit /b 1
)
echo ✅ Config Server construido exitosamente
cd ..

echo.
echo [2/4] Construyendo Discovery Server...
cd discovery-server
docker build -t library/discovery-server:latest .
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo al construir discovery-server
    exit /b 1
)
echo ✅ Discovery Server construido exitosamente
cd ..

echo.
echo [3/4] Construyendo MS-Book...
cd ms-book
docker build -t library/ms-book:latest .
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo al construir ms-book
    exit /b 1
)
echo ✅ MS-Book construido exitosamente
cd ..

echo.
echo [4/4] Construyendo Gateway...
cd gateway
docker build -t library/gateway:latest .
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Fallo al construir gateway
    exit /b 1
)
echo ✅ Gateway construido exitosamente
cd ..

echo.
echo ========================================
echo     TODAS LAS IMAGENES CONSTRUIDAS
echo ========================================
echo.
echo Verificando imágenes creadas:
docker images | findstr library

echo.
echo Para ejecutar con Docker Compose:
echo docker-compose up -d

pause