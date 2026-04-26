# AGENTS.md

## Contexto del proyecto

Proyecto `monitoreo-casas` con arquitectura de 2 servicios Spring Boot:

- `edge-detector` (`8081`): recibe detecciones, valida reglas (`label=person`, `confidence`, `cooldown`) y publica evento a notifier.
- `notifier-service` (`8082`): recibe eventos, guarda historial en memoria, expone dashboard web y envía notificaciones (log/email/whatsapp).

## Estado funcional actual

- Flujo end-to-end operativo: dashboard/camara -> `edge-detector` -> `notifier-service`.
- Dashboard disponible en `http://localhost:8082/`.
- Detección en navegador con TensorFlow.js + coco-ssd.
- Notificación por correo implementada y validada (requiere App Password de Google).
- WhatsApp Cloud API implementado por configuración.

## Comandos correctos de arranque (Windows PowerShell)

> Ejecutar desde la raiz del repo: `C:\projects\monitoreo-casas`

### 1) notifier-service (con correo)

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

### 2) edge-detector (sin Docker Compose local)

```powershell
cd c:\projects\monitoreo-casas
$env:SPRING_DOCKER_COMPOSE_ENABLED="false"
.\edge-detector\mvnw.cmd -f edge-detector\pom.xml spring-boot:run
```

## Comandos para apagar servicios

```powershell
$p8081 = Get-NetTCPConnection -LocalPort 8081 -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
if ($p8081) { $p8081 | ForEach-Object { Stop-Process -Id $_ -Force } }

$p8082 = Get-NetTCPConnection -LocalPort 8082 -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
if ($p8082) { $p8082 | ForEach-Object { Stop-Process -Id $_ -Force } }
```

## Validaciones rápidas

- Health:
  - `http://localhost:8081/actuator/health`
  - `http://localhost:8082/actuator/health`
- Dashboard:
  - `http://localhost:8082/`
- Evento manual:

```powershell
$body = '{"cameraId":"cam-01","label":"person","confidence":0.90,"zone":"entrada","imageRef":"manual"}'
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/detections/test" -Method Post -ContentType "application/json" -Body $body
```

## Problemas conocidos y solución

1. **`.\edge-detector\mvnw.cmd` no se reconoce**
   - Causa: terminal abierta en carpeta incorrecta.
   - Solución: ubicarse en `C:\projects\monitoreo-casas` o usar `..\edge-detector\mvnw.cmd` desde `notifier-service`.

2. **Falla `edge-detector` por Docker Compose**
   - Error típico: `dockerDesktopLinuxEngine... cannot find the file specified`.
   - Solución: exportar `$env:SPRING_DOCKER_COMPOSE_ENABLED="false"` antes de arrancar.

3. **Correo no llega (Gmail)**
   - Error típico: `Application-specific password required`.
   - Solución: activar 2FA y usar App Password de 16 caracteres en `MAIL_PASSWORD`.

## Archivos clave

- `edge-detector/src/main/resources/application.yaml`
- `edge-detector/src/main/java/com/monitoreo/edge/service/DetectionProcessingService.java`
- `notifier-service/src/main/resources/application.yaml`
- `notifier-service/src/main/resources/static/index.html`
- `notifier-service/src/main/java/com/monitoreo/notifier/notification/EmailNotificationGateway.java`
- `notifier-service/src/main/java/com/monitoreo/notifier/notification/WhatsappNotificationGateway.java`

## Convenciones para próximos cambios

- No hardcodear secretos en código ni en `application.yaml`.
- Usar variables de entorno para credenciales.
- Mantener comandos de arranque con `mvnw` desde la raíz para evitar incompatibilidades de Maven global.
- Si se agrega persistencia real, reemplazar store en memoria por JPA/PostgreSQL sin romper endpoints actuales.
