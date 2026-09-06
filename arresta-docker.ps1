$ErrorActionPreference = 'Stop'

if ($env:DOCKER_HOST -eq 'tcp://localhost:2375') {
    Remove-Item Env:DOCKER_HOST
}

docker compose --file (Join-Path $PSScriptRoot 'compose.yaml') down

Write-Host 'Container arrestati. I dati e i segreti locali sono stati conservati.' -ForegroundColor Green
