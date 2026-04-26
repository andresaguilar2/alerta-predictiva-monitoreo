Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$env:SPRING_DOCKER_COMPOSE_ENABLED = "false"

Write-Host "Iniciando edge-detector..." -ForegroundColor Cyan
& ".\edge-detector\mvnw.cmd" -f "edge-detector\pom.xml" spring-boot:run
