$ErrorActionPreference = 'Stop'

if ($env:DOCKER_HOST -eq 'tcp://localhost:2375') {
    Remove-Item Env:DOCKER_HOST
}

$possibiliCartelleFrontend = @(
    (Join-Path $PSScriptRoot '..\gestione-ordini-frontend'),
    (Join-Path $PSScriptRoot '..\gestioneOrdiniFrontend')
)
$cartellaFrontend = $possibiliCartelleFrontend |
    Where-Object { Test-Path -LiteralPath (Join-Path $_ 'Dockerfile') } |
    Select-Object -First 1

if (-not $cartellaFrontend) {
    throw 'Il repository del frontend con il relativo Dockerfile non è stato trovato accanto al backend.'
}

$env:FRONTEND_CONTEXT = (Resolve-Path -LiteralPath $cartellaFrontend).Path

docker version --format 'Docker Engine {{.Server.Version}}' | Out-Host
docker compose --file (Join-Path $PSScriptRoot 'compose.yaml') up --build --detach --wait

Write-Host ''
Write-Host 'Gestione Ordini è disponibile:' -ForegroundColor Green
Write-Host '  Applicazione: http://localhost:5173'
Write-Host '  API:          http://localhost:8081'
Write-Host '  Swagger:      http://localhost:8081/swagger-ui.html'
