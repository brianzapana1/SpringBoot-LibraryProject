#!/bin/bash

# Keycloak Realm Setup Script
echo "=== Setting up Keycloak Realm and Users ==="

# Wait for Keycloak to be ready
echo "Waiting for Keycloak to start..."
sleep 30

# Keycloak admin CLI setup
KEYCLOAK_URL="http://localhost:8180"
REALM_NAME="library-realm"
CLIENT_ID="library-client"

# Get admin access token
echo "Getting admin access token..."
ADMIN_TOKEN=$(curl -s -X POST "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin" \
  -d "password=admin123" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | jq -r '.access_token')

if [ "$ADMIN_TOKEN" = "null" ] || [ -z "$ADMIN_TOKEN" ]; then
    echo "Failed to get admin token. Is Keycloak running?"
    exit 1
fi

echo "Admin token obtained successfully"

# Create realm
echo "Creating realm: $REALM_NAME"
curl -s -X POST "${KEYCLOAK_URL}/admin/realms" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "realm": "'$REALM_NAME'",
    "displayName": "Library Management Realm",
    "enabled": true,
    "accessTokenLifespan": 3600,
    "refreshTokenMaxReuse": 0,
    "sslRequired": "external"
  }'

# Create client
echo "Creating client: $CLIENT_ID"
curl -s -X POST "${KEYCLOAK_URL}/admin/realms/$REALM_NAME/clients" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "'$CLIENT_ID'",
    "name": "Library Management Client",
    "enabled": true,
    "directAccessGrantsEnabled": true,
    "serviceAccountsEnabled": true,
    "publicClient": false,
    "protocol": "openid-connect",
    "attributes": {
      "access.token.lifespan": "3600"
    }
  }'

# Get client UUID
CLIENT_UUID=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/$REALM_NAME/clients?clientId=$CLIENT_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[0].id')

# Set client secret
echo "Setting client secret..."
curl -s -X PUT "${KEYCLOAK_URL}/admin/realms/$REALM_NAME/clients/$CLIENT_UUID" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "'$CLIENT_ID'",
    "secret": "library-client-secret"
  }'

# Create roles
echo "Creating roles..."
curl -s -X POST "${KEYCLOAK_URL}/admin/realms/$REALM_NAME/roles" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "USER",
    "description": "Regular user role for library system"
  }'

curl -s -X POST "${KEYCLOAK_URL}/admin/realms/$REALM_NAME/roles" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "ADMIN",
    "description": "Administrator role for library system"
  }'

# Create test users
echo "Creating test users..."

# Create admin user
curl -s -X POST "${KEYCLOAK_URL}/admin/realms/$REALM_NAME/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "libraryadmin",
    "email": "admin@library.com",
    "firstName": "Library",
    "lastName": "Administrator",
    "enabled": true,
    "credentials": [{
      "type": "password",
      "value": "admin123",
      "temporary": false
    }]
  }'

# Get admin user ID
ADMIN_USER_ID=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/$REALM_NAME/users?username=libraryadmin" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[0].id')

# Assign ADMIN role to admin user
curl -s -X POST "${KEYCLOAK_URL}/admin/realms/$REALM_NAME/users/$ADMIN_USER_ID/role-mappings/realm" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[{
    "name": "ADMIN"
  }]'

# Create regular user
curl -s -X POST "${KEYCLOAK_URL}/admin/realms/$REALM_NAME/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "libraryuser",
    "email": "user@library.com",
    "firstName": "Library",
    "lastName": "User",
    "enabled": true,
    "credentials": [{
      "type": "password",
      "value": "user123",
      "temporary": false
    }]
  }'

# Get regular user ID
USER_ID=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/$REALM_NAME/users?username=libraryuser" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[0].id')

# Assign USER role to regular user
curl -s -X POST "${KEYCLOAK_URL}/admin/realms/$REALM_NAME/users/$USER_ID/role-mappings/realm" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '[{
    "name": "USER"
  }]'

echo "=== Keycloak Setup Complete ==="
echo "Realm: $REALM_NAME"
echo "Admin User: libraryadmin / admin123"
echo "Regular User: libraryuser / user123"
echo "Client: $CLIENT_ID / library-client-secret"
echo "Access Keycloak Admin Console: $KEYCLOAK_URL/admin"