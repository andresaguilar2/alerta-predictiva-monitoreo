# Comandos para iniciar servidores

Guia rapida para levantar `notifier-service` (8082) y `edge-detector` (8081) en Windows PowerShell.

## Requisitos

- Estar en la raiz del proyecto: `C:\projects\monitoreo-casas`
- Tener Java 21 instalado

## 1) Iniciar notifier-service (con correo habilitado)

```powershell
cd c:\projects\monitoreo-casas
$env:NOTIFY_EMAIL_ENABLED="true"
$env:NOTIFY_EMAIL_FROM="tu-correo@gmail.com"
$env:NOTIFY_EMAIL_TO="destino@gmail.com"
$env:MAIL_HOST="smtp.gmail.com"
$env:MAIL_PORT="587"
$env:MAIL_USERNAME="tu-correo@gmail.com"
$env:MAIL_PASSWORD="tu-app-password"
.\edge-detector\mvnw.cmd -f notifier-service\pom.xml spring-boot:run
```

## 2) Iniciar edge-detector (sin Docker Compose local)

Abre otra terminal y ejecuta:

```powershell
cd c:\projects\monitoreo-casas
$env:SPRING_DOCKER_COMPOSE_ENABLED="false"
.\edge-detector\mvnw.cmd -f edge-detector\pom.xml spring-boot:run
```

## Validaciones rapidas

- Health edge: `http://localhost:8081/actuator/health`
- Health notifier: `http://localhost:8082/actuator/health`
- Dashboard: `http://localhost:8082/`

## Prueba manual de evento

```powershell
$body = '{"cameraId":"cam-01","label":"person","confidence":0.90,"zone":"entrada","imageRef":"manual"}'
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/detections/test" -Method Post -ContentType "application/json" -Body $body
```

## Apagar servidores

```powershell
$p8081 = Get-NetTCPConnection -LocalPort 8081 -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
if ($p8081) { $p8081 | ForEach-Object { Stop-Process -Id $_ -Force } }

$p8082 = Get-NetTCPConnection -LocalPort 8082 -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
if ($p8082) { $p8082 | ForEach-Object { Stop-Process -Id $_ -Force } }
```
