#!/bin/bash

echo "🔐 Configurando Keycloak para Angular Frontend con Authorization Code Flow..."

# Variables de configuración
KEYCLOAK_URL="http://localhost:8180"
ADMIN_USER="admin"
ADMIN_PASSWORD="admin123"
REALM_NAME="biblioteca-realm"
FRONTEND_CLIENT_ID="biblioteca-frontend"
BACKEND_CLIENT_ID="biblioteca-backend"

# Función para obtener token de admin
get_admin_token() {
    curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "username=$ADMIN_USER" \
        -d "password=$ADMIN_PASSWORD" \
        -d "grant_type=password" \
        -d "client_id=admin-cli" | jq -r '.access_token'
}

# Esperar a que Keycloak esté listo
echo "⏳ Esperando a que Keycloak esté disponible..."
until curl -f "$KEYCLOAK_URL/health/ready" > /dev/null 2>&1; do
    echo "Esperando a Keycloak..."
    sleep 10
done

echo "✅ Keycloak está disponible"

# Obtener token de administrador
echo "🔑 Obteniendo token de administrador..."
ADMIN_TOKEN=$(get_admin_token)

if [ -z "$ADMIN_TOKEN" ] || [ "$ADMIN_TOKEN" = "null" ]; then
    echo "❌ Error: No se pudo obtener el token de administrador"
    exit 1
fi

echo "✅ Token de administrador obtenido"

# Crear realm
echo "🏛️ Creando realm '$REALM_NAME'..."
curl -s -X POST "$KEYCLOAK_URL/admin/realms" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "realm": "'$REALM_NAME'",
        "enabled": true,
        "displayName": "Biblioteca Realm",
        "registrationAllowed": true,
        "resetPasswordAllowed": true,
        "rememberMe": true,
        "loginWithEmailAllowed": true,
        "duplicateEmailsAllowed": false,
        "accessTokenLifespan": 300,
        "ssoSessionMaxLifespan": 3600,
        "accessCodeLifespan": 60,
        "accessCodeLifespanUserAction": 300,
        "registrationEmailAsUsername": false
    }' > /dev/null

echo "✅ Realm '$REALM_NAME' creado"

# Crear client para Frontend Angular (Authorization Code Flow)
echo "🖥️ Creando client para Frontend Angular..."
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/clients" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "clientId": "'$FRONTEND_CLIENT_ID'",
        "name": "Biblioteca Frontend Angular",
        "description": "Cliente para el frontend Angular con Authorization Code Flow",
        "enabled": true,
        "clientAuthenticatorType": "client-secret",
        "secret": "frontend-secret-12345",
        "redirectUris": [
            "http://localhost:4200/*",
            "http://localhost:4200/callback",
            "http://localhost:4200/silent-refresh.html"
        ],
        "webOrigins": [
            "http://localhost:4200"
        ],
        "protocol": "openid-connect",
        "publicClient": false,
        "bearerOnly": false,
        "serviceAccountsEnabled": false,
        "directAccessGrantsEnabled": true,
        "authorizationServicesEnabled": false,
        "standardFlowEnabled": true,
        "implicitFlowEnabled": false,
        "directAccessGrantsEnabled": true,
        "attributes": {
            "access.token.lifespan": "300",
            "pkce.code.challenge.method": "S256"
        },
        "protocolMappers": [
            {
                "name": "audience-mapper",
                "protocol": "openid-connect",
                "protocolMapper": "oidc-audience-mapper",
                "config": {
                    "included.client.audience": "'$BACKEND_CLIENT_ID'",
                    "access.token.claim": "true"
                }
            },
            {
                "name": "roles-mapper",
                "protocol": "openid-connect", 
                "protocolMapper": "oidc-usermodel-realm-role-mapper",
                "config": {
                    "claim.name": "roles",
                    "jsonType.label": "String",
                    "multivalued": "true",
                    "access.token.claim": "true",
                    "id.token.claim": "true",
                    "userinfo.token.claim": "true"
                }
            }
        ]
    }' > /dev/null

echo "✅ Client Frontend creado"

# Crear client para Backend (Resource Server)
echo "🔧 Creando client para Backend..."
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/clients" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "clientId": "'$BACKEND_CLIENT_ID'",
        "name": "Biblioteca Backend Services",
        "description": "Cliente para los microservicios backend",
        "enabled": true,
        "clientAuthenticatorType": "client-secret",
        "secret": "backend-secret-12345",
        "protocol": "openid-connect",
        "publicClient": false,
        "bearerOnly": true,
        "serviceAccountsEnabled": false,
        "directAccessGrantsEnabled": false,
        "authorizationServicesEnabled": false,
        "standardFlowEnabled": false,
        "implicitFlowEnabled": false
    }' > /dev/null

echo "✅ Client Backend creado"

# Crear roles
echo "👥 Creando roles..."

# Rol de Administrador
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "admin",
        "description": "Administrador del sistema"
    }' > /dev/null

# Rol de Usuario
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "user",
        "description": "Usuario estándar"
    }' > /dev/null

# Rol de Empleado
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "empleado",
        "description": "Empleado de la biblioteca"
    }' > /dev/null

echo "✅ Roles creados (admin, user, empleado)"

# Crear usuario administrador
echo "👤 Creando usuario administrador..."
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "admin",
        "email": "admin@biblioteca.com",
        "firstName": "Admin",
        "lastName": "Biblioteca",
        "enabled": true,
        "emailVerified": true,
        "credentials": [
            {
                "type": "password",
                "value": "admin123",
                "temporary": false
            }
        ]
    }' > /dev/null

# Obtener ID del usuario admin para asignar roles
ADMIN_USER_ID=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users?username=admin" \
    -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[0].id')

# Obtener ID del rol admin
ADMIN_ROLE=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles/admin" \
    -H "Authorization: Bearer $ADMIN_TOKEN")

# Asignar rol admin al usuario
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users/$ADMIN_USER_ID/role-mappings/realm" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d "[$ADMIN_ROLE]" > /dev/null

echo "✅ Usuario administrador creado y configurado"

# Crear usuario estándar
echo "👤 Creando usuario estándar..."
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "usuario1",
        "email": "usuario1@biblioteca.com",
        "firstName": "Juan",
        "lastName": "Pérez",
        "enabled": true,
        "emailVerified": true,
        "credentials": [
            {
                "type": "password",
                "value": "user123",
                "temporary": false
            }
        ]
    }' > /dev/null

# Obtener ID del usuario estándar
USER_ID=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users?username=usuario1" \
    -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[0].id')

# Obtener ID del rol user
USER_ROLE=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles/user" \
    -H "Authorization: Bearer $ADMIN_TOKEN")

# Asignar rol user al usuario
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users/$USER_ID/role-mappings/realm" \
    -H "Authorization: Bearer $ADMIN_TOKEN" \
    -H "Content-Type: application/json" \
    -d "[$USER_ROLE]" > /dev/null

echo "✅ Usuario estándar creado y configurado"

echo ""
echo "🎉 ¡Configuración de Keycloak completada!"
echo ""
echo "📋 RESUMEN DE CONFIGURACIÓN:"
echo "============================="
echo "🌐 Keycloak URL: $KEYCLOAK_URL"
echo "🏛️  Realm: $REALM_NAME"
echo ""
echo "👥 CLIENTS CONFIGURADOS:"
echo "========================"
echo "🖥️  Frontend Client:"
echo "   - ID: $FRONTEND_CLIENT_ID"
echo "   - Tipo: Authorization Code Flow"
echo "   - Secret: frontend-secret-12345"
echo "   - Redirect URIs: http://localhost:4200/*"
echo ""
echo "🔧 Backend Client:"
echo "   - ID: $BACKEND_CLIENT_ID"
echo "   - Tipo: Bearer Only"
echo "   - Secret: backend-secret-12345"
echo ""
echo "👤 USUARIOS DE PRUEBA:"
echo "======================"
echo "🔑 Admin:"
echo "   - Username: admin"
echo "   - Password: admin123"
echo "   - Role: admin"
echo ""
echo "👨 Usuario:"
echo "   - Username: usuario1"
echo "   - Password: user123"
echo "   - Role: user"
echo ""
echo "🔗 URLs IMPORTANTES:"
echo "==================="
echo "🌐 Consola Admin: $KEYCLOAK_URL/admin"
echo "🔐 Realm URL: $KEYCLOAK_URL/realms/$REALM_NAME"
echo "🔑 Token Endpoint: $KEYCLOAK_URL/realms/$REALM_NAME/protocol/openid-connect/token"
echo "👤 Auth Endpoint: $KEYCLOAK_URL/realms/$REALM_NAME/protocol/openid-connect/auth"
echo ""