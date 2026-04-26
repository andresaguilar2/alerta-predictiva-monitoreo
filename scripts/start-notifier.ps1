Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$env:NOTIFY_EMAIL_ENABLED = "true"
$env:NOTIFY_EMAIL_FROM = "escobarmorcillo@gmail.com"
$env:NOTIFY_EMAIL_TO = "escobarmorcillo@gmail.com"
$env:MAIL_HOST = "smtp.gmail.com"
$env:MAIL_PORT = "587"
$env:MAIL_USERNAME = "escobarmorcillo@gmail.com"
$env:MAIL_PASSWORD = "cjon egga vmsj cvlr"

Write-Host "Iniciando notifier-service..." -ForegroundColor Cyan
& ".\edge-detector\mvnw.cmd" -f "notifier-service\pom.xml" spring-boot:run
