param(
    [string]$Message = "hello world",
    [string]$EncryptKey,
    [string]$ConfigUrl = "https://localhost:8443/config",
    [string]$User = "dev-usr",
    [string]$Password = "dev-pwd"
)

# ENCRYPT_KEY is required by Config Server to decrypt; set it for this session if provided
if ([string]::IsNullOrWhiteSpace($EncryptKey)) {
    Write-Warning "Define ENCRYPT_KEY (param -EncryptKey or pre-set env var) before usar /encrypt y /decrypt."
} else {
    $env:ENCRYPT_KEY = $EncryptKey
    Write-Host "ENCRYPT_KEY configurada en la sesión actual." -ForegroundColor Cyan
}

# Basic auth header (use explicit string expansion to avoid ':' parsing issue)
$pair = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("$($User):$($Password)"))
$headers = @{ Authorization = "Basic $pair" }

function Invoke-ConfigPost([string]$Endpoint, [string]$Body) {
    Invoke-RestMethod -Method Post -Uri "$ConfigUrl/$Endpoint" -Headers $headers -SkipCertificateCheck -ContentType "text/plain" -Body $Body -ErrorAction Stop
}

Write-Host "1) Enviando /encrypt con mensaje: '$Message'" -ForegroundColor Green
$encrypted = $null
try {
    $encrypted = Invoke-ConfigPost -Endpoint "encrypt" -Body $Message
    Write-Host "Cipher: $encrypted" -ForegroundColor Yellow
} catch {
    Write-Error "Fallo /encrypt: $($_.Exception.Message)"; exit 1
}

Write-Host "2) Enviando /decrypt con el cipher anterior" -ForegroundColor Green
$decrypted = $null
try {
    $decrypted = Invoke-ConfigPost -Endpoint "decrypt" -Body $encrypted
    Write-Host "Plaintext devuelto: $decrypted" -ForegroundColor Yellow
} catch {
    Write-Error "Fallo /decrypt: $($_.Exception.Message)"; exit 1
}

Write-Host "3) (Opcional) ENCRYPT_KEY actual: $env:ENCRYPT_KEY" -ForegroundColor Cyan
