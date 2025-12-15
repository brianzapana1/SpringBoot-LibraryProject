param(
    [string]$ConfigServerUrl = "http://localhost:8888",
    [string]$Username = "config",
    [string]$Password = "config123",
    [string]$PlainText = "book_pass",
    [string]$Cipher = $null,
    [switch]$SkipConfigFetch
)

function New-BasicAuthHeader {
    param(
        [string]$User,
        [string]$Pass
    )

    $token = [System.Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("$User:$Pass"))
    return @{ Authorization = "Basic $token" }
}

function Test-ConfigServerHealth {
    param(
        [string]$BaseUrl,
        [hashtable]$Headers
    )

    Write-Host "Verificando salud de Config Server en $BaseUrl..." -ForegroundColor Cyan
    $health = Invoke-RestMethod -Uri "$BaseUrl/actuator/health" -Headers $Headers -ErrorAction Stop
    Write-Host "Estado: $($health.status)" -ForegroundColor Green
}

function Invoke-Encrypt {
    param(
        [string]$BaseUrl,
        [hashtable]$Headers,
        [string]$Secret
    )

    Write-Host "Solicitando nuevo cipher para el valor plano..." -ForegroundColor Cyan
    $cipher = Invoke-RestMethod -Uri "$BaseUrl/encrypt" -Method Post -Headers ($Headers + @{ 'Content-Type' = 'text/plain' }) -Body $Secret -ErrorAction Stop
    Write-Host "Cipher generado: $cipher" -ForegroundColor Green
    return $cipher
}

function Invoke-Decrypt {
    param(
        [string]$BaseUrl,
        [hashtable]$Headers,
        [string]$CipherValue
    )

    Write-Host "Verificando desencriptación..." -ForegroundColor Cyan
    $plain = Invoke-RestMethod -Uri "$BaseUrl/decrypt" -Method Post -Headers ($Headers + @{ 'Content-Type' = 'text/plain' }) -Body $CipherValue -ErrorAction Stop
    Write-Host "Valor recuperado: $plain" -ForegroundColor Green
    return $plain
}

function Get-MsBookPassword {
    param(
        [string]$BaseUrl,
        [hashtable]$Headers
    )

    Write-Host "Obteniendo configuración de ms-book..." -ForegroundColor Cyan
    $config = Invoke-RestMethod -Uri "$BaseUrl/ms-book/default" -Headers $Headers -ErrorAction Stop
    $password = $config.propertySources[0].source.'spring.datasource.password'
    Write-Host "Password entregado por Config Server: $password" -ForegroundColor Green
    return $password
}

try {
    $headers = New-BasicAuthHeader -User $Username -Pass $Password

    Test-ConfigServerHealth -BaseUrl $ConfigServerUrl -Headers $headers

    if (-not $Cipher) {
        $Cipher = Invoke-Encrypt -BaseUrl $ConfigServerUrl -Headers $headers -Secret $PlainText
    }
    else {
        Write-Host "Usando cipher proporcionado: $Cipher" -ForegroundColor Yellow
    }

    $decrypted = Invoke-Decrypt -BaseUrl $ConfigServerUrl -Headers $headers -CipherValue $Cipher

    if ($decrypted -ne $PlainText) {
        throw "La desencriptación resultó en '$decrypted' y no coincide con el valor esperado '$PlainText'."
    }

    if (-not $SkipConfigFetch) {
        $configPassword = Get-MsBookPassword -BaseUrl $ConfigServerUrl -Headers $headers

        if ($configPassword -ne $PlainText) {
            Write-Warning "El password servido por Config Server ('$configPassword') no coincide con el valor plano esperado ('$PlainText')."
        }
    }

    Write-Host "\nVerificación completada exitosamente." -ForegroundColor Green
    Write-Host "Cipher final: {cipher}$Cipher" -ForegroundColor Green
}
catch {
    Write-Error "Error durante la verificación: $_"
    exit 1
}
