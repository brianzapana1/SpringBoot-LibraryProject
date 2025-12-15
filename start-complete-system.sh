#!/bin/bash

echo "🚀 Iniciando sistema completo Keycloak + Microservicios + Frontend Angular"

# Función para verificar si un puerto está disponible
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null ; then
        echo "⚠️ Puerto $1 está en uso"
        return 1
    else
        echo "✅ Puerto $1 está disponible"
        return 0
    fi
}

# Verificar puertos necesarios
echo "🔍 Verificando puertos..."
check_port 8180  # Keycloak
check_port 8761  # Eureka
check_port 8888  # Config Server
check_port 8080  # Gateway
check_port 8081  # MS-Book
check_port 5432  # PostgreSQL
check_port 4200  # Angular

# Paso 1: Iniciar infraestructura backend
echo "🔧 Paso 1: Iniciando infraestructura de microservicios..."
cd "$(dirname "$0")"

# Iniciar servicios de Docker
echo "📦 Iniciando servicios de Docker (PostgreSQL, Keycloak, Eureka, Config Server)..."
docker-compose up -d book-postgres keycloak discovery-server config-server

# Esperar a que Keycloak esté listo
echo "⏳ Esperando a que Keycloak esté disponible..."
sleep 30
until curl -f http://localhost:8180/health/ready > /dev/null 2>&1; do
    echo "Esperando a Keycloak..."
    sleep 10
done

# Paso 2: Configurar Keycloak
echo "🔐 Paso 2: Configurando Keycloak..."
if command -v bash &> /dev/null; then
    bash ./keycloak-setup-frontend.sh
else
    echo "❌ Bash no disponible, configure Keycloak manualmente"
fi

# Paso 3: Iniciar Gateway y MS-Book
echo "🔧 Paso 3: Iniciando Gateway y Microservicios..."
docker-compose up -d gateway ms-book

# Esperar a que los servicios estén listos
echo "⏳ Esperando a que los servicios estén listos..."
sleep 30

# Verificar estado de servicios
echo "📊 Verificando estado de servicios..."
services=("discovery-server:8761/eureka" "config-server:8888/actuator/health" "gateway:8080/actuator/health" "keycloak:8180/health/ready")

for service in "${services[@]}"; do
    IFS=':' read -r name port_path <<< "$service"
    if curl -f "http://localhost:$port_path" > /dev/null 2>&1; then
        echo "✅ $name está funcionando"
    else
        echo "❌ $name no responde"
    fi
done

# Paso 4: Instalar dependencias de Angular e iniciar
echo "🌐 Paso 4: Preparando Frontend Angular..."
cd "../TallerSisFront-desarrollo"

# Verificar si Node.js está disponible
if ! command -v npm &> /dev/null; then
    echo "❌ npm no está disponible. Por favor instale Node.js"
    exit 1
fi

# Instalar dependencias
echo "📦 Instalando dependencias de Angular..."
npm install

# Iniciar servidor de desarrollo
echo "🚀 Iniciando servidor Angular en http://localhost:4200"
npm start &
ANGULAR_PID=$!

# Información final
echo ""
echo "🎉 ¡Sistema iniciado!"
echo "=============================="
echo "🌐 URLs importantes:"
echo "• Frontend Angular: http://localhost:4200"
echo "• Keycloak Admin: http://localhost:8180/admin"
echo "• Eureka Dashboard: http://localhost:8761"
echo "• Gateway: http://localhost:8080"
echo "• Config Server: http://localhost:8888"
echo ""
echo "🔑 Credenciales de prueba:"
echo "• Admin Keycloak: admin / admin123"
echo "• Usuario de prueba: admin / admin123"
echo "• Usuario estándar: usuario1 / user123"
echo ""
echo "📋 Para probar:"
echo "1. Abra http://localhost:4200"
echo "2. Haga clic en 'Iniciar Sesión (Keycloak)'"
echo "3. Use las credenciales de prueba"
echo "4. Vaya a 'Test Keycloak+Gateway' para probar la integración"
echo ""
echo "🛑 Para detener todo: Ctrl+C y luego ejecute: docker-compose down"
echo ""

# Esperar señal de salida
trap 'kill $ANGULAR_PID; docker-compose down; exit' INT
wait