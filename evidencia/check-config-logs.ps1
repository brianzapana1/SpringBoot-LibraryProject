param(
    [int]$Tail = 300,
    [switch]$HitApps,
    [switch]$CheckEndpoint,
    [string]$Service = "ms-book",
    [string]$ConfigUrl = "http://localhost:8888"
)

# Location of the compose project
$proj = Resolve-Path "$PSScriptRoot"

# Ensure docker compose can run
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Error "docker not found in PATH"
    exit 1
}

Push-Location $proj

try {
    # Optionally trigger config fetches for ms-book and gateway
    if ($HitApps) {
        Write-Host "Haciendo ping a endpoints de config (ms-book y gateway)..."
        $null = Invoke-WebRequest -UseBasicParsing "http://localhost:8888/ms-book/default" -ErrorAction SilentlyContinue
        $null = Invoke-WebRequest -UseBasicParsing "http://localhost:8888/gateway/default" -ErrorAction SilentlyContinue
    }

    if ($CheckEndpoint) {
        $url = "$ConfigUrl/$Service/default"
        Write-Host "Verificando endpoint $url ..." -ForegroundColor Green
        try {
            $resp = Invoke-WebRequest -UseBasicParsing $url -ErrorAction Stop
            Write-Host "Status: $($resp.StatusCode) $($resp.StatusDescription)" -ForegroundColor Yellow
        } catch {
            Write-Error "Fallo al consultar $url : $($_.Exception.Message)"; throw
        }
    }

    Write-Host "Mostrando logs del config-server (tail $Tail) filtrados a carga de config y fetch Git..." -ForegroundColor Cyan
    # application.yml = YML general compartido; ms-book.yml y gateway.yml = YML específicos por servicio
    docker compose logs config-server --tail=$Tail |
        Select-String -Pattern "Added property source|Located environment|PacketLine|fetch|ls-refs|symref|want|application.yml|ms-book|gateway|master"
}
finally {
    Pop-Location
}
