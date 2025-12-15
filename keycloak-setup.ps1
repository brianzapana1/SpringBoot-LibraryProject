# Keycloak Realm Setup Script (PowerShell)
Write-Host "=== Setting up Keycloak Realm and Users ===" -ForegroundColor Green

# Configuration
$KEYCLOAK_URL = "http://localhost:8180"
$REALM_NAME = "library-realm"
$CLIENT_ID = "library-client"

# Wait for Keycloak to be ready
Write-Host "Waiting for Keycloak to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# Get admin access token
Write-Host "Getting admin access token..." -ForegroundColor Yellow
$tokenBody = @{
    username = "admin"
    password = "admin123"
    grant_type = "password"
    client_id = "admin-cli"
}

try {
    $tokenResponse = Invoke-RestMethod -Uri "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" -Method Post -Body $tokenBody -ContentType "application/x-www-form-urlencoded"
    $ADMIN_TOKEN = $tokenResponse.access_token
    Write-Host "Admin token obtained successfully" -ForegroundColor Green
}
catch {
    Write-Host "Failed to get admin token. Is Keycloak running?" -ForegroundColor Red
    exit 1
}

# Headers for authenticated requests
$headers = @{
    "Authorization" = "Bearer $ADMIN_TOKEN"
    "Content-Type" = "application/json"
}

# Create realm
Write-Host "Creating realm: $REALM_NAME" -ForegroundColor Yellow
$realmData = @{
    realm = $REALM_NAME
    displayName = "Library Management Realm"
    enabled = $true
    accessTokenLifespan = 3600
    refreshTokenMaxReuse = 0
    sslRequired = "external"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms" -Method Post -Body $realmData -Headers $headers
    Write-Host "Realm created successfully" -ForegroundColor Green
}
catch {
    Write-Host "Realm may already exist or failed to create" -ForegroundColor Yellow
}

# Create client
Write-Host "Creating client: $CLIENT_ID" -ForegroundColor Yellow
$clientData = @{
    clientId = $CLIENT_ID
    name = "Library Management Client"
    enabled = $true
    directAccessGrantsEnabled = $true
    serviceAccountsEnabled = $true
    publicClient = $false
    protocol = "openid-connect"
    secret = "library-client-secret"
    attributes = @{
        "access.token.lifespan" = "3600"
    }
} | ConvertTo-Json -Depth 3

try {
    Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM_NAME/clients" -Method Post -Body $clientData -Headers $headers
    Write-Host "Client created successfully" -ForegroundColor Green
}
catch {
    Write-Host "Client may already exist or failed to create" -ForegroundColor Yellow
}

# Create roles
Write-Host "Creating roles..." -ForegroundColor Yellow
$userRole = @{
    name = "USER"
    description = "Regular user role for library system"
} | ConvertTo-Json

$adminRole = @{
    name = "ADMIN"
    description = "Administrator role for library system"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles" -Method Post -Body $userRole -Headers $headers
    Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles" -Method Post -Body $adminRole -Headers $headers
    Write-Host "Roles created successfully" -ForegroundColor Green
}
catch {
    Write-Host "Roles may already exist or failed to create" -ForegroundColor Yellow
}

# Create test users
Write-Host "Creating test users..." -ForegroundColor Yellow

# Create admin user
$adminUser = @{
    username = "libraryadmin"
    email = "admin@library.com"
    firstName = "Library"
    lastName = "Administrator"
    enabled = $true
    credentials = @(
        @{
            type = "password"
            value = "admin123"
            temporary = $false
        }
    )
} | ConvertTo-Json -Depth 3

try {
    Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users" -Method Post -Body $adminUser -Headers $headers
    Write-Host "Admin user created" -ForegroundColor Green

    # Get admin user ID and assign role
    $adminUserResponse = Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users?username=libraryadmin" -Method Get -Headers $headers
    $adminUserId = $adminUserResponse[0].id
    
    $adminRoleMapping = @(
        @{ name = "ADMIN" }
    ) | ConvertTo-Json

    Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users/$adminUserId/role-mappings/realm" -Method Post -Body $adminRoleMapping -Headers $headers
    Write-Host "Admin role assigned" -ForegroundColor Green
}
catch {
    Write-Host "Admin user may already exist or failed to create" -ForegroundColor Yellow
}

# Create regular user
$regularUser = @{
    username = "libraryuser"
    email = "user@library.com"
    firstName = "Library"
    lastName = "User"
    enabled = $true
    credentials = @(
        @{
            type = "password"
            value = "user123"
            temporary = $false
        }
    )
} | ConvertTo-Json -Depth 3

try {
    Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users" -Method Post -Body $regularUser -Headers $headers
    Write-Host "Regular user created" -ForegroundColor Green

    # Get regular user ID and assign role
    $userResponse = Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users?username=libraryuser" -Method Get -Headers $headers
    $userId = $userResponse[0].id
    
    $userRoleMapping = @(
        @{ name = "USER" }
    ) | ConvertTo-Json

    Invoke-RestMethod -Uri "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users/$userId/role-mappings/realm" -Method Post -Body $userRoleMapping -Headers $headers
    Write-Host "User role assigned" -ForegroundColor Green
}
catch {
    Write-Host "Regular user may already exist or failed to create" -ForegroundColor Yellow
}

Write-Host "=== Keycloak Setup Complete ===" -ForegroundColor Green
Write-Host "Realm: $REALM_NAME" -ForegroundColor Cyan
Write-Host "Admin User: libraryadmin / admin123" -ForegroundColor Cyan
Write-Host "Regular User: libraryuser / user123" -ForegroundColor Cyan
Write-Host "Client: $CLIENT_ID / library-client-secret" -ForegroundColor Cyan
Write-Host "Access Keycloak Admin Console: $KEYCLOAK_URL/admin" -ForegroundColor Cyan