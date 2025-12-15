# Script de Testing para Eureka Service Discovery
# Ejecutar después de iniciar ambos servicios (Eureka Server y MS-Book)

Write-Host "===========================================" -ForegroundColor Green
Write-Host "   EUREKA SERVICE DISCOVERY - TEST SUITE" -ForegroundColor Green
Write-Host "===========================================" -ForegroundColor Green
Write-Host ""

# Test 1: Verificar Eureka Server
Write-Host "1. Verificando Eureka Server..." -ForegroundColor Yellow
try {
    $eurekaResponse = Invoke-RestMethod -Uri "http://localhost:8761/actuator/health" -Method GET
    Write-Host "   ✅ Eureka Server: ACTIVO" -ForegroundColor Green
    Write-Host "   Status: $($eurekaResponse.status)" -ForegroundColor Cyan
} catch {
    Write-Host "   ❌ Eureka Server: NO DISPONIBLE" -ForegroundColor Red
    Write-Host "   Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 2: Verificar MS-Book
Write-Host "2. Verificando MS-Book..." -ForegroundColor Yellow
try {
    $bookResponse = Invoke-RestMethod -Uri "http://localhost:8082/actuator/health" -Method GET
    Write-Host "   ✅ MS-Book: ACTIVO" -ForegroundColor Green
    Write-Host "   Status: $($bookResponse.status)" -ForegroundColor Cyan
} catch {
    Write-Host "   ❌ MS-Book: NO DISPONIBLE" -ForegroundColor Red
    Write-Host "   Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 3: Listar servicios registrados
Write-Host "3. Probando Discovery: Servicios Registrados..." -ForegroundColor Yellow
try {
    $servicesResponse = Invoke-RestMethod -Uri "http://localhost:8082/discovery/services" -Method GET
    Write-Host "   ✅ Discovery funcional" -ForegroundColor Green
    Write-Host "   Servicios encontrados: $($servicesResponse.total_count)" -ForegroundColor Cyan
    Write-Host "   Lista: $($servicesResponse.registered_services -join ', ')" -ForegroundColor Cyan
} catch {
    Write-Host "   ❌ Discovery endpoint no disponible" -ForegroundColor Red
    Write-Host "   Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 4: Instancias de MS-BOOK
Write-Host "4. Probando instancias del servicio MS-BOOK..." -ForegroundColor Yellow
try {
    $instancesResponse = Invoke-RestMethod -Uri "http://localhost:8082/discovery/services/MS-BOOK/instances" -Method GET
    Write-Host "   ✅ Instancias encontradas: $($instancesResponse.instance_count)" -ForegroundColor Green
    if ($instancesResponse.instances.Count -gt 0) {
        $instance = $instancesResponse.instances[0]
        Write-Host "   Instancia 1: $($instance.host):$($instance.port)" -ForegroundColor Cyan
        Write-Host "   Instance ID: $($instance.instance_id)" -ForegroundColor Cyan
    }
} catch {
    Write-Host "   ❌ Error obteniendo instancias" -ForegroundColor Red
    Write-Host "   Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 5: Comunicación usando nombres lógicos
Write-Host "5. Probando comunicación via Service Discovery..." -ForegroundColor Yellow
try {
    $communicationResponse = Invoke-RestMethod -Uri "http://localhost:8082/discovery/test/self-communication" -Method GET
    Write-Host "   ✅ Comunicación exitosa usando nombre lógico" -ForegroundColor Green
    Write-Host "   Método: $($communicationResponse.method)" -ForegroundColor Cyan
    Write-Host "   Endpoint usado: $($communicationResponse.endpoint_called)" -ForegroundColor Cyan
    Write-Host "   Resultado: $($communicationResponse.result)" -ForegroundColor Cyan
} catch {
    Write-Host "   ❌ Error en comunicación via discovery" -ForegroundColor Red
    Write-Host "   Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 6: Test completo
Write-Host "6. Ejecutando test completo de descubrimiento..." -ForegroundColor Yellow
try {
    $completeTestResponse = Invoke-RestMethod -Uri "http://localhost:8082/discovery/test/complete" -Method GET
    Write-Host "   ✅ Test completo exitoso" -ForegroundColor Green
    Write-Host "   Total servicios: $($completeTestResponse.total_services)" -ForegroundColor Cyan
    Write-Host "   Método de discovery: $($completeTestResponse.discovery_method)" -ForegroundColor Cyan
} catch {
    Write-Host "   ❌ Error en test completo" -ForegroundColor Red
    Write-Host "   Error: $_" -ForegroundColor Red
}
Write-Host ""

# Información adicional
Write-Host "===========================================" -ForegroundColor Green
Write-Host "           INFORMACIÓN ADICIONAL" -ForegroundColor Green
Write-Host "===========================================" -ForegroundColor Green
Write-Host "📊 Eureka Dashboard: http://localhost:8761" -ForegroundColor Cyan
Write-Host "📚 MS-Book API: http://localhost:8082/books" -ForegroundColor Cyan
Write-Host "🔍 Discovery Endpoints: http://localhost:8082/discovery" -ForegroundColor Cyan
Write-Host "💾 PostgreSQL: localhost:5432/librarydb" -ForegroundColor Cyan
Write-Host ""
Write-Host "🎯 FUNCIONALIDADES IMPLEMENTADAS:" -ForegroundColor Green
Write-Host "   ✅ Registro automático en Eureka" -ForegroundColor White
Write-Host "   ✅ Descubrimiento dinámico de servicios" -ForegroundColor White
Write-Host "   ✅ Comunicación usando nombres lógicos" -ForegroundColor White
Write-Host "   ✅ Load balancing con WebClient" -ForegroundColor White
Write-Host "   ✅ Dashboard de monitoreo Eureka" -ForegroundColor White
Write-Host "   ✅ Heartbeat automático (30s)" -ForegroundColor White
Write-Host ""
Write-Host "===========================================" -ForegroundColor Green