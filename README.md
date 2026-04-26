# Monitoreo de casas - MVP Spring Boot

Proyecto base con dos servicios:

- `edge-detector`: recibe una deteccion y aplica reglas (label `person`, umbral, cooldown).
- `notifier-service`: recibe eventos, registra en memoria y simula envio de notificacion.

## Requisitos

- JDK 21
- Maven 3.9+ (o Maven Wrapper en `edge-detector`)
- Docker (opcional)

## Ejecutar local (sin Docker)

1. Levantar `notifier-service`:

```powershell
cd notifier-service
mvn spring-boot:run
```

2. Levantar `edge-detector` en otra terminal:

```powershell
cd edge-detector
.\mvnw.cmd spring-boot:run
```

## Probar flujo end-to-end

Enviar deteccion al edge:

```powershell
curl -X POST http://localhost:8081/api/v1/detections/test `
  -H "Content-Type: application/json" `
  -d "{\"cameraId\":\"cam-01\",\"label\":\"person\",\"confidence\":0.85,\"zone\":\"entrada\",\"imageRef\":\"snapshot-1.jpg\"}"
```

Consultar eventos recibidos por notifier:

```powershell
curl http://localhost:8082/api/v1/events
```

## Ejecutar con Docker

Desde la raiz:

```powershell
docker compose up --build
```

## Notificaciones por email y WhatsApp

`notifier-service` ya soporta multiples canales.

### Email SMTP

Configura variables de entorno antes de iniciar `notifier-service`:

```powershell
$env:NOTIFY_EMAIL_ENABLED="true"
$env:NOTIFY_EMAIL_FROM="tu-correo@gmail.com"
$env:NOTIFY_EMAIL_TO="destino@gmail.com"
$env:MAIL_HOST="smtp.gmail.com"
$env:MAIL_PORT="587"
$env:MAIL_USERNAME="tu-correo@gmail.com"
$env:MAIL_PASSWORD="tu-app-password"
```

### WhatsApp Cloud API (Meta)

```powershell
$env:NOTIFY_WHATSAPP_ENABLED="true"
$env:WHATSAPP_API_URL="https://graph.facebook.com/v20.0"
$env:WHATSAPP_ACCESS_TOKEN="EAAG..."
$env:WHATSAPP_PHONE_NUMBER_ID="1234567890"
$env:WHATSAPP_TO="573001112233"
```

Con estos valores, cada evento de persona detectada intentara enviar por los canales habilitados.
