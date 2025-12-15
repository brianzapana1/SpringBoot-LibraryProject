Write-Host "🚀 Iniciando sistema completo Keycloak + Microservicios + Frontend Angular" -ForegroundColor Green

# Función para verificar si un puerto está en uso
function Test-Port {
    param([int]$Port)
    try {
        $connection = Test-NetConnection -ComputerName "localhost" -Port $Port -WarningAction SilentlyContinue
        if ($connection.TcpTestSucceeded) {
            Write-Host "⚠️ Puerto $Port está en uso" -ForegroundColor Yellow
            return $false
        } else {
            Write-Host "✅ Puerto $Port está disponible" -ForegroundColor Green
            return $true
        }
    } catch {
        Write-Host "✅ Puerto $Port está disponible" -ForegroundColor Green
        return $true
    }
}

# Verificar puertos necesarios
Write-Host "🔍 Verificando puertos..." -ForegroundColor Cyan
$ports = @(8180, 8761, 8888, 8080, 8081, 5432, 4200)
foreach ($port in $ports) {
    Test-Port -Port $port | Out-Null
}

# Paso 1: Iniciar infraestructura backend
Write-Host "🔧 Paso 1: Iniciando infraestructura de microservicios..." -ForegroundColor Cyan

# Verificar si Docker está disponible
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "❌ Docker no está disponible. Por favor instale Docker Desktop" -ForegroundColor Red
    exit 1
}

# Iniciar servicios de Docker
Write-Host "📦 Iniciando servicios de Docker (PostgreSQL, Keycloak, Eureka, Config Server)..." -ForegroundColor Yellow
docker-compose up -d book-postgres keycloak discovery-server config-server

# Esperar a que Keycloak esté listo
Write-Host "⏳ Esperando a que Keycloak esté disponible..." -ForegroundColor Yellow
Start-Sleep 30

$maxAttempts = 12
$attempt = 0
do {
    $attempt++
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8180/health/ready" -TimeoutSec 10 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ Keycloak está disponible" -ForegroundColor Green
            break
        }
    } catch {
        Write-Host "Esperando a Keycloak... (intento $attempt/$maxAttempts)" -ForegroundColor Yellow
        Start-Sleep 10
    }
} while ($attempt -lt $maxAttempts)

if ($attempt -eq $maxAttempts) {
    Write-Host "❌ Timeout esperando a Keycloak" -ForegroundColor Red
    exit 1
}

# Paso 2: Configurar Keycloak
Write-Host "🔐 Paso 2: Configurando Keycloak..." -ForegroundColor Cyan
Write-Host "⚠️ Configurando Keycloak automáticamente..." -ForegroundColor Yellow

# Configuración básica de Keycloak usando PowerShell
$keycloakUrl = "http://localhost:8180"
$adminUser = "admin"
$adminPassword = "admin123"

try {
    # Obtener token de admin
    $tokenResponse = Invoke-RestMethod -Uri "$keycloakUrl/realms/master/protocol/openid-connect/token" -Method Post -Body @{
        username = $adminUser
        password = $adminPassword
        grant_type = "password"
        client_id = "admin-cli"
    } -ContentType "application/x-www-form-urlencoded"
    
    $adminToken = $tokenResponse.access_token
    Write-Host "✅ Token de administrador obtenido" -ForegroundColor Green
    
    # Crear realm
    $realmData = @{
        realm = "biblioteca-realm"
        enabled = $true
        displayName = "Biblioteca Realm"
        registrationAllowed = $true
    } | ConvertTo-Json
    
    try {
        Invoke-RestMethod -Uri "$keycloakUrl/admin/realms" -Method Post -Headers @{Authorization="Bearer $adminToken"} -Body $realmData -ContentType "application/json"
        Write-Host "✅ Realm 'biblioteca-realm' creado" -ForegroundColor Green
    } catch {
        Write-Host "⚠️ Realm ya existe o error al crear" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "⚠️ Error configurando Keycloak automáticamente. Configure manualmente en http://localhost:8180/admin" -ForegroundColor Yellow
}

# Paso 3: Iniciar Gateway y MS-Book
Write-Host "🔧 Paso 3: Iniciando Gateway y Microservicios..." -ForegroundColor Cyan
docker-compose up -d gateway ms-book

# Esperar a que los servicios estén listos
Write-Host "⏳ Esperando a que los servicios estén listos..." -ForegroundColor Yellow
Start-Sleep 30

# Verificar estado de servicios
Write-Host "📊 Verificando estado de servicios..." -ForegroundColor Cyan
$services = @{
    "Eureka" = "http://localhost:8761/eureka"
    "Config Server" = "http://localhost:8888/actuator/health"
    "Gateway" = "http://localhost:8080/actuator/health"
    "Keycloak" = "http://localhost:8180/health/ready"
}

foreach ($service in $services.GetEnumerator()) {
    try {
        $response = Invoke-WebRequest -Uri $service.Value -TimeoutSec 10
        if ($response.StatusCode -eq 200) {
            Write-Host "✅ $($service.Key) está funcionando" -ForegroundColor Green
        }
    } catch {
        Write-Host "❌ $($service.Key) no responde" -ForegroundColor Red
    }
}

# Paso 4: Preparar Frontend Angular
Write-Host "🌐 Paso 4: Preparando Frontend Angular..." -ForegroundColor Cyan

# Verificar si npm está disponible
if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    Write-Host "❌ npm no está disponible. Por favor instale Node.js" -ForegroundColor Red
    Write-Host "Descargue desde: https://nodejs.org/" -ForegroundColor Yellow
    
    # Continuar sin Angular
    Write-Host "📋 Backend está listo. Configure Angular manualmente:" -ForegroundColor Yellow
    Write-Host "1. Instale Node.js" -ForegroundColor White
    Write-Host "2. cd TallerSisFront-desarrollo" -ForegroundColor White
    Write-Host "3. npm install" -ForegroundColor White
    Write-Host "4. npm start" -ForegroundColor White
} else {
    # Cambiar a directorio de Angular
    $angularPath = "..\TallerSisFront-desarrollo"
    if (Test-Path $angularPath) {
        Set-Location $angularPath
        
        # Instalar dependencias
        Write-Host "📦 Instalando dependencias de Angular..." -ForegroundColor Yellow
        npm install
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Dependencias instaladas correctamente" -ForegroundColor Green
            
            # Iniciar servidor de desarrollo
            Write-Host "🚀 Iniciando servidor Angular en http://localhost:4200..." -ForegroundColor Green
            Start-Process -FilePath "npm" -ArgumentList "start" -NoNewWindow
            
            # Esperar un poco para que Angular inicie
            Start-Sleep 10
        } else {
            Write-Host "❌ Error instalando dependencias de Angular" -ForegroundColor Red
        }
    } else {
        Write-Host "❌ No se encontró el directorio del frontend Angular" -ForegroundColor Red
    }
}

# Información final
Write-Host ""
Write-Host "🎉 ¡Sistema iniciado!" -ForegroundColor Green
Write-Host "==============================" -ForegroundColor White
Write-Host "🌐 URLs importantes:" -ForegroundColor Cyan
Write-Host "• Frontend Angular: http://localhost:4200" -ForegroundColor White
Write-Host "• Keycloak Admin: http://localhost:8180/admin" -ForegroundColor White
Write-Host "• Eureka Dashboard: http://localhost:8761" -ForegroundColor White
Write-Host "• Gateway: http://localhost:8080" -ForegroundColor White
Write-Host "• Config Server: http://localhost:8888" -ForegroundColor White
Write-Host ""
Write-Host "🔑 Credenciales de prueba:" -ForegroundColor Cyan
Write-Host "• Admin Keycloak: admin / admin123" -ForegroundColor White
Write-Host "• Usuario de prueba: admin / admin123" -ForegroundColor White
Write-Host "• Usuario estándar: usuario1 / user123" -ForegroundColor White
Write-Host ""
Write-Host "📋 Para probar:" -ForegroundColor Cyan
Write-Host "1. Configure Keycloak manualmente en http://localhost:8180/admin" -ForegroundColor White
Write-Host "2. Cree realm 'biblioteca-realm'" -ForegroundColor White
Write-Host "3. Cree client 'biblioteca-frontend' (Authorization Code Flow)" -ForegroundColor White
Write-Host "4. Cree usuarios y roles" -ForegroundColor White
Write-Host "5. Abra http://localhost:4200" -ForegroundColor White
Write-Host "6. Haga clic en 'Iniciar Sesión (Keycloak)'" -ForegroundColor White
Write-Host "7. Vaya a 'Test Keycloak+Gateway' para probar la integración" -ForegroundColor White
Write-Host ""
Write-Host "🛑 Para detener todo: docker-compose down" -ForegroundColor Red
Write-Host ""

Write-Host "✨ Presione cualquier tecla para continuar o Ctrl+C para salir..." -ForegroundColor Yellow
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")