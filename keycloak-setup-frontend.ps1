Write-Host "🔐 Configuración automática de Keycloak para Frontend Angular" -ForegroundColor Green
Write-Host "=============================================================" -ForegroundColor White

function Get-KeycloakStatusCode {
    param([System.Management.Automation.ErrorRecord]$ErrorRecord)

    if ($ErrorRecord -and $ErrorRecord.Exception -and $ErrorRecord.Exception.Response) {
        try {
            return [int]$ErrorRecord.Exception.Response.StatusCode
        } catch {
            return $null
        }
    }

    return $null
}

function Get-ListFromEnv {
    param(
        [string[]]$DefaultValues,
        [string]$OverrideValue
    )

    if (-not [string]::IsNullOrWhiteSpace($OverrideValue)) {
        return $OverrideValue.Split(",") |
            ForEach-Object { $_.Trim() } |
            Where-Object { $_ }
    }

    return $DefaultValues
}

function Test-KeycloakAvailability {
    param(
        [string]$BaseUrl,
        [int]$TimeoutSec = 10
    )

    $probes = @(
        "$BaseUrl/health/ready",
        "$BaseUrl/realms/master/.well-known/openid-configuration",
        $BaseUrl
    )

    foreach ($probe in $probes) {
        try {
            $response = Invoke-WebRequest -Uri $probe -TimeoutSec $TimeoutSec
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {
                return @{
                    Success = $true
                    Endpoint = $probe
                    StatusCode = $response.StatusCode
                }
            }
        } catch {
            $statusCode = Get-KeycloakStatusCode $_

            if ($probe -like "*/health/ready" -and $statusCode -eq 404) {
                continue
            }
        }
    }

    return @{ Success = $false }
}

function Get-KeycloakClientById {
    param(
        [string]$BaseUrl,
        [string]$Realm,
        [string]$ClientId,
        [hashtable]$Headers
    )

    $encodedClientId = [System.Uri]::EscapeDataString($ClientId)
    $clientLookupUri = "$BaseUrl/admin/realms/$Realm/clients?clientId=$encodedClientId"

    try {
        $response = Invoke-RestMethod -Uri $clientLookupUri -Method Get -Headers $Headers -ErrorAction Stop
        if (-not $response) {
            return $null
        }

        $asArray = @($response)
        if ($asArray.Count -gt 0) {
            return $asArray[0]
        }

        return $null
    } catch {
        return $null
    }
}

function Get-KeycloakUserByUsername {
    param(
        [string]$BaseUrl,
        [string]$Realm,
        [string]$Username,
        [hashtable]$Headers
    )

    $encodedUsername = [System.Uri]::EscapeDataString($Username)
    $userLookupUri = "$BaseUrl/admin/realms/$Realm/users?username=$encodedUsername&exact=true"

    try {
        $response = Invoke-RestMethod -Uri $userLookupUri -Method Get -Headers $Headers -ErrorAction Stop
        if (-not $response) {
            return $null
        }

        $asArray = @($response)
        if ($asArray.Count -gt 0) {
            return $asArray[0]
        }

        return $null
    } catch {
        return $null
    }
}

function Get-KeycloakErrorBody {
    param([System.Management.Automation.ErrorRecord]$ErrorRecord)

    if (-not $ErrorRecord) {
        return $null
    }

    if ($ErrorRecord.ErrorDetails -and -not [string]::IsNullOrWhiteSpace($ErrorRecord.ErrorDetails.Message)) {
        return $ErrorRecord.ErrorDetails.Message
    }

    if (-not $ErrorRecord.Exception -or -not $ErrorRecord.Exception.Response) {
        return $null
    }

    try {
        $stream = $ErrorRecord.Exception.Response.GetResponseStream()
        if (-not $stream -or -not $stream.CanRead) {
            return $null
        }

        if ($stream.CanSeek) {
            $stream.Position = 0
        }

        $reader = New-Object System.IO.StreamReader($stream)
        try {
            $body = $reader.ReadToEnd()
        } finally {
            $reader.Dispose()
        }

        return if ([string]::IsNullOrWhiteSpace($body)) { $null } else { $body }
    } catch {
        return $null
    }
}

$keycloakUrl = if ($env:KEYCLOAK_URL) { $env:KEYCLOAK_URL } else { "http://localhost:8180" }
$adminUser = if ($env:KEYCLOAK_ADMIN_USER) { $env:KEYCLOAK_ADMIN_USER } else { "admin" }
$adminPassword = if ($env:KEYCLOAK_ADMIN_PASSWORD) { $env:KEYCLOAK_ADMIN_PASSWORD } else { "admin123" }
$realmName = if ($env:KEYCLOAK_REALM) { $env:KEYCLOAK_REALM } else { "DemoUCB" }
$frontendClientId = if ($env:KEYCLOAK_FRONTEND_CLIENT) { $env:KEYCLOAK_FRONTEND_CLIENT } else { "angular-app" }
$backendClientId = if ($env:KEYCLOAK_BACKEND_CLIENT) { $env:KEYCLOAK_BACKEND_CLIENT } else { "biblioteca-backend" }
$frontendRedirectUris = Get-ListFromEnv -DefaultValues @("http://localhost:4200/*", "http://localhost:4200", "http://localhost:4200/callback") -OverrideValue $env:KEYCLOAK_FRONTEND_REDIRECTS
$frontendOrigins = Get-ListFromEnv -DefaultValues @("http://localhost:4200") -OverrideValue $env:KEYCLOAK_FRONTEND_ORIGINS
$frontendPostLogoutUris = Get-ListFromEnv -DefaultValues @("http://localhost:4200/*") -OverrideValue $env:KEYCLOAK_FRONTEND_POST_LOGOUT

if ($keycloakUrl.EndsWith("/")) {
    $keycloakUrl = $keycloakUrl.TrimEnd('/')
}

Write-Host "🔍 Verificando conexión con Keycloak..." -ForegroundColor Cyan

$availability = Test-KeycloakAvailability -BaseUrl $keycloakUrl

if (-not $availability.Success) {
    Write-Host "❌ Keycloak no responde en $keycloakUrl" -ForegroundColor Red
    Write-Host "Verifique que el contenedor esté levantado y que el puerto 8180 no esté bloqueado" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Keycloak respondió desde $($availability.Endpoint) (HTTP $($availability.StatusCode))" -ForegroundColor Green
Write-Host "ℹ️  La pantalla de login está disponible en $keycloakUrl/admin" -ForegroundColor Cyan

Write-Host "🔑 Obteniendo token de administrador..." -ForegroundColor Cyan

try {
    # Obtener token de admin
    $tokenData = @{
        username = $adminUser
        password = $adminPassword
        grant_type = "password"
        client_id = "admin-cli"
    }
    
    $tokenResponse = Invoke-RestMethod -Uri "$keycloakUrl/realms/master/protocol/openid-connect/token" -Method Post -Body $tokenData -ContentType "application/x-www-form-urlencoded"
    $adminToken = $tokenResponse.access_token
    Write-Host "✅ Token obtenido exitosamente" -ForegroundColor Green
} catch {
    Write-Host "❌ Error obteniendo token de administrador" -ForegroundColor Red
    Write-Host "Verifique las credenciales: $adminUser / $adminPassword" -ForegroundColor Yellow
    exit 1
}

# Headers para requests
$headers = @{
    "Authorization" = "Bearer $adminToken"
    "Content-Type" = "application/json"
}

Write-Host "🏰 Creando realm '$realmName'..." -ForegroundColor Cyan

$realmData = @{
    realm = $realmName
    enabled = $true
    displayName = "Sistema de Biblioteca"
    registrationAllowed = $true
    passwordPolicy = "length(8)"
    loginTheme = "keycloak"
    accountTheme = "keycloak"
    emailTheme = "keycloak"
    sslRequired = "none"
    accessTokenLifespan = 3600
    accessTokenLifespanForImplicitFlow = 900
    ssoSessionIdleTimeout = 1800
    ssoSessionMaxLifespan = 36000
    offlineSessionIdleTimeout = 2592000
    rememberMe = $true
} | ConvertTo-Json -Depth 10

try {
    Invoke-RestMethod -Uri "$keycloakUrl/admin/realms" -Method Post -Headers $headers -Body $realmData
    Write-Host "✅ Realm '$realmName' creado exitosamente" -ForegroundColor Green
} catch {
    $statusCode = Get-KeycloakStatusCode $_
    if ($statusCode -eq 409) {
        Write-Host "⚠️ Realm '$realmName' ya existe" -ForegroundColor Yellow
    } else {
        Write-Host "❌ Error creando realm '$realmName': $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
}

Write-Host "👤 Creando roles del sistema..." -ForegroundColor Cyan

$roles = @(
    @{ name = "admin"; description = "Administrador del sistema con acceso completo" },
    @{ name = "empleado"; description = "Empleado de la biblioteca" },
    @{ name = "user"; description = "Usuario estándar del sistema" }
)

foreach ($role in $roles) {
    try {
        $roleData = $role | ConvertTo-Json
        Invoke-RestMethod -Uri "$keycloakUrl/admin/realms/$realmName/roles" -Method Post -Headers $headers -Body $roleData
        Write-Host "✅ Rol '$($role.name)' creado" -ForegroundColor Green
    } catch {
        $statusCode = Get-KeycloakStatusCode $_
        if ($statusCode -eq 409) {
            Write-Host "⚠️ Rol '$($role.name)' ya existe" -ForegroundColor Yellow
        } else {
            Write-Host "❌ Error creando rol '$($role.name)': $($_.Exception.Message)" -ForegroundColor Red
            exit 1
        }
    }
}

Write-Host "🖥️ Creando cliente para Frontend Angular..." -ForegroundColor Cyan

$frontendClientData = @{
    clientId = $frontendClientId
    name = "Biblioteca Frontend Angular"
    description = "Cliente Angular para el sistema de biblioteca"
    enabled = $true
    publicClient = $true
    directAccessGrantsEnabled = $false
    standardFlowEnabled = $true
    implicitFlowEnabled = $false
    serviceAccountsEnabled = $false
    protocol = "openid-connect"
    redirectUris = $frontendRedirectUris
    webOrigins = $frontendOrigins
    attributes = @{
        "pkce.code.challenge.method" = "S256"
        "post.logout.redirect.uris" = ($frontendPostLogoutUris -join "`n")
        "oauth2.device.authorization.grant.enabled" = $false
        "oidc.ciba.grant.enabled" = $false
    }
} | ConvertTo-Json -Depth 10

$existingFrontendClient = Get-KeycloakClientById -BaseUrl $keycloakUrl -Realm $realmName -ClientId $frontendClientId -Headers $headers

if ($existingFrontendClient) {
    Write-Host "⚠️ Cliente '$frontendClientId' ya existe (ID: $($existingFrontendClient.id))" -ForegroundColor Yellow
} else {
    try {
        Invoke-RestMethod -Uri "$keycloakUrl/admin/realms/$realmName/clients" -Method Post -Headers $headers -Body $frontendClientData
        Write-Host "✅ Cliente '$frontendClientId' creado exitosamente" -ForegroundColor Green
    } catch {
        $statusCode = Get-KeycloakStatusCode $_
        if ($statusCode -eq 409) {
            Write-Host "⚠️ Cliente '$frontendClientId' ya existe" -ForegroundColor Yellow
        } else {
            Write-Host "❌ Error creando cliente frontend: $($_.Exception.Message)" -ForegroundColor Red
            $errorBody = Get-KeycloakErrorBody $_
            if ($errorBody) {
                Write-Host "Detalle: $errorBody" -ForegroundColor Yellow
            }
            exit 1
        }
    }
}

Write-Host "🔧 Creando cliente para Backend (Resource Server)..." -ForegroundColor Cyan

$backendClientData = @{
    clientId = $backendClientId
    name = "Biblioteca Backend Resource Server"
    description = "Resource server para validar tokens"
    enabled = $true
    publicClient = $false
    bearerOnly = $true
    standardFlowEnabled = $false
    directAccessGrantsEnabled = $false
    serviceAccountsEnabled = $true
    protocol = "openid-connect"
} | ConvertTo-Json -Depth 10

$existingBackendClient = Get-KeycloakClientById -BaseUrl $keycloakUrl -Realm $realmName -ClientId $backendClientId -Headers $headers

if ($existingBackendClient) {
    Write-Host "⚠️ Cliente '$backendClientId' ya existe (ID: $($existingBackendClient.id))" -ForegroundColor Yellow
} else {
    try {
        Invoke-RestMethod -Uri "$keycloakUrl/admin/realms/$realmName/clients" -Method Post -Headers $headers -Body $backendClientData
        Write-Host "✅ Cliente '$backendClientId' creado exitosamente" -ForegroundColor Green
    } catch {
        $statusCode = Get-KeycloakStatusCode $_
        if ($statusCode -eq 409) {
            Write-Host "⚠️ Cliente '$backendClientId' ya existe" -ForegroundColor Yellow
        } else {
            Write-Host "❌ Error creando cliente backend: $($_.Exception.Message)" -ForegroundColor Red
            $errorBody = Get-KeycloakErrorBody $_
            if ($errorBody) {
                Write-Host "Detalle: $errorBody" -ForegroundColor Yellow
            }
            exit 1
        }
    }
}

Write-Host "👥 Creando usuarios de prueba..." -ForegroundColor Cyan

$users = @(
    @{
        username = "admin"
        firstName = "Administrador"
        lastName = "Sistema"
        email = "admin@biblioteca.com"
        enabled = $true
        emailVerified = $true
        roles = @("admin", "empleado", "user")
        password = "Admin123!"
    },
    @{
        username = "empleado1"
        firstName = "Carlos"
        lastName = "Bibliotecario"
        email = "empleado1@biblioteca.com"
        enabled = $true
        emailVerified = $true
        roles = @("empleado", "user")
        password = "Empleado123!"
    },
    @{
        username = "usuario1"
        firstName = "María"
        lastName = "Lectora"
        email = "usuario1@biblioteca.com"
        enabled = $true
        emailVerified = $true
        roles = @("user")
        password = "Usuario123!"
    }
)

foreach ($user in $users) {
    $userId = $null
    $existingUser = Get-KeycloakUserByUsername -BaseUrl $keycloakUrl -Realm $realmName -Username $user.username -Headers $headers

    if ($existingUser) {
        Write-Host "⚠️ Usuario '$($user.username)' ya existe" -ForegroundColor Yellow
        $userId = $existingUser.id
    } else {
        $userData = @{
            username = $user.username
            firstName = $user.firstName
            lastName = $user.lastName
            email = $user.email
            enabled = $user.enabled
            emailVerified = $user.emailVerified
        } | ConvertTo-Json

        try {
            Invoke-RestMethod -Uri "$keycloakUrl/admin/realms/$realmName/users" -Method Post -Headers $headers -Body $userData
            Write-Host "✅ Usuario '$($user.username)' creado" -ForegroundColor Green
            $createdUser = Get-KeycloakUserByUsername -BaseUrl $keycloakUrl -Realm $realmName -Username $user.username -Headers $headers
            if ($createdUser) {
                $userId = $createdUser.id
            }
        } catch {
            $statusCode = Get-KeycloakStatusCode $_
            if ($statusCode -eq 409) {
                Write-Host "⚠️ Usuario '$($user.username)' ya existe" -ForegroundColor Yellow
                $existingUser = Get-KeycloakUserByUsername -BaseUrl $keycloakUrl -Realm $realmName -Username $user.username -Headers $headers
                if ($existingUser) {
                    $userId = $existingUser.id
                }
            } else {
                Write-Host "❌ Error creando usuario '$($user.username)': $($_.Exception.Message)" -ForegroundColor Red
                $errorBody = Get-KeycloakErrorBody $_
                if ($errorBody) {
                    Write-Host "Detalle: $errorBody" -ForegroundColor Yellow
                }
                exit 1
            }
        }
    }

    if (-not $userId) {
        Write-Host "⚠️ No se pudo obtener el ID del usuario '$($user.username)'. Se omiten pasos adicionales." -ForegroundColor Yellow
        continue
    }

    # Establecer contraseña (idempotente)
    $passwordData = @{
        type = "password"
        value = $user.password
        temporary = $false
    } | ConvertTo-Json

    try {
        Invoke-RestMethod -Uri "$keycloakUrl/admin/realms/$realmName/users/$userId/reset-password" -Method Put -Headers $headers -Body $passwordData
        Write-Host "✅ Contraseña establecida para '$($user.username)'" -ForegroundColor Green
    } catch {
        Write-Host "⚠️ Error configurando la contraseña de '$($user.username)': $($_.Exception.Message)" -ForegroundColor Yellow
    }

    # Asignar roles
    $existingRoleNames = @()
    try {
        $existingRoleMappings = Invoke-RestMethod -Uri "$keycloakUrl/admin/realms/$realmName/users/$userId/role-mappings/realm" -Method Get -Headers $headers
        if ($existingRoleMappings) {
            $existingRoleNames = @($existingRoleMappings | ForEach-Object { $_.name })
        }
    } catch {
        $existingRoleNames = @()
    }

    foreach ($roleName in $user.roles) {
        if ($existingRoleNames -contains $roleName) {
            Write-Host "⚠️ Rol '$roleName' ya estaba asignado a '$($user.username)'" -ForegroundColor Yellow
            continue
        }

        try {
            $roleResponse = Invoke-RestMethod -Uri "$keycloakUrl/admin/realms/$realmName/roles/$roleName" -Method Get -Headers $headers
            $roleJson = $roleResponse | ConvertTo-Json -Depth 5
            $roleData = "[$roleJson]"

            Invoke-RestMethod -Uri "$keycloakUrl/admin/realms/$realmName/users/$userId/role-mappings/realm" -Method Post -Headers $headers -ContentType "application/json" -Body $roleData
            Write-Host "✅ Rol '$roleName' asignado a '$($user.username)'" -ForegroundColor Green
            $existingRoleNames += $roleName
        } catch {
            Write-Host "⚠️ Error asignando rol '$roleName' a '$($user.username)': $($_.Exception.Message)" -ForegroundColor Yellow
            $roleErrorBody = Get-KeycloakErrorBody $_
            if ($roleErrorBody) {
                Write-Host "Detalle: $roleErrorBody" -ForegroundColor DarkYellow
            }
        }
    }
}

Write-Host ""
Write-Host "🎉 ¡Configuración de Keycloak completada!" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor White
Write-Host ""
Write-Host "🌐 Información de configuración:" -ForegroundColor Cyan
Write-Host "• Realm: $realmName" -ForegroundColor White
Write-Host "• Frontend Client ID: $frontendClientId" -ForegroundColor White
Write-Host "• Backend Client ID: $backendClientId" -ForegroundColor White
Write-Host "• Authorization Code Flow con PKCE habilitado" -ForegroundColor White
Write-Host ""
Write-Host "👤 Usuarios de prueba creados:" -ForegroundColor Cyan
Write-Host "• admin / Admin123! (roles: admin, empleado, user)" -ForegroundColor White
Write-Host "• empleado1 / Empleado123! (roles: empleado, user)" -ForegroundColor White
Write-Host "• usuario1 / Usuario123! (roles: user)" -ForegroundColor White
Write-Host ""
Write-Host "🔗 URLs importantes:" -ForegroundColor Cyan
Write-Host "• Keycloak Admin: $keycloakUrl/admin" -ForegroundColor White
Write-Host "• Login URL: $keycloakUrl/realms/$realmName/protocol/openid-connect/auth" -ForegroundColor White
Write-Host "• Token URL: $keycloakUrl/realms/$realmName/protocol/openid-connect/token" -ForegroundColor White
Write-Host ""
Write-Host "⚙️ Configuración Angular ya lista en:" -ForegroundColor Cyan
Write-Host "• app.config.ts" -ForegroundColor White
Write-Host "• keycloak-auth.service.ts" -ForegroundColor White
Write-Host "• auth.guard.ts" -ForegroundColor White
Write-Host ""
Write-Host "📱 Próximos pasos:" -ForegroundColor Cyan
Write-Host "1. cd TallerSisFront-desarrollo" -ForegroundColor White
Write-Host "2. npm install" -ForegroundColor White
Write-Host "3. npm start" -ForegroundColor White
Write-Host "4. Abrir http://localhost:4200" -ForegroundColor White
Write-Host "5. Hacer clic en 'Iniciar Sesión (Keycloak)'" -ForegroundColor White
Write-Host ""