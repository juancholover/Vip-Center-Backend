# 🏋️ VIP Center Fit — Backend Skills & Context

## 📋 Resumen del Proyecto

**VIP Center Fit** es un sistema de gestión integral para un gimnasio real en Perú. El sistema controla clientes, membresías, pagos (MercadoPago + Yape + Efectivo), asistencias (por QR), reportes y notificaciones.

- **Stack:** Spring Boot 3.4.10, Java 17+, MySQL (BD: `gym`), JPA/Hibernate, Maven
- **Puerto:** `localhost:8080`
- **Moneda:** PEN (Soles peruanos)
- **Zona horaria:** America/Lima (UTC-5)

---

## 🗄️ Arquitectura del Proyecto

```
com.gimnasio.fit/
├── config/          → Seguridad (JWT, CORS, Spring Security), DataInitializer
├── controller/      → 15 controladores REST
├── dto/             → 62 DTOs (request/response)
├── entity/          → 18 entidades JPA
├── repository/      → 17 repositorios Spring Data
├── service/         → 22 servicios de negocio
├── serviceImpl/     → Implementaciones de servicios
├── specification/   → JPA Specifications (filtros dinámicos)
├── util/            → Utilidades
└── utils/           → Utilidades adicionales
```

---

## 🔐 Sistema de Roles y Seguridad

- **Autenticación:** JWT (Bearer token), refresh token
- **Roles existentes:** `ADMIN`, `RECEPCIONISTA` (el cliente los llama "Administrador" y "Secretaria")
- **El admin puede crear más roles** (confirmado por el cliente en la reunión)
- **Permisos granulares:** Sistema de permisos por rol (tabla `rol_permisos`)
- **Login:** Bloqueo tras 5 intentos fallidos, bloqueo de 24h

---

## 📦 Entidades Principales

### Cliente (`clientes`)
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | PK auto |
| nombre | String(100) | Obligatorio |
| apellido | String(100) | Obligatorio |
| telefono | String(20) | Obligatorio |
| email | String(100) | Opcional |
| qr_acceso | String(64) | QR único, se genera al primer pago |
| qr_activo | Boolean | true por defecto |
| fecha_vencimiento | LocalDate | Se actualiza al pagar |
| membresia_id | FK→Membresia | Membresía actual |
| registrado_por | FK→Usuario | Quién lo registró |
| fecha_registro | Instant | Auto (CreationTimestamp) |
| estado_seguimiento | String(20) | "PENDIENTE"/"LLAMADO"/"PROMESA" (HU-32) |

**⚠️ NO tiene campo `dni` — el cliente lo solicitó en la reunión**

**Estado calculado (`@Transient getEstado()`):**
- `"activo"` → fecha_vencimiento >= HOY && qr_activo = true
- `"vencido"` → fecha_vencimiento < HOY
- `"sin_membresia"` → fecha_vencimiento IS NULL (**⚠️ el cliente pidió cambiar a "vencido"**)
- `"qr_deshabilitado"` → qr_activo = false

### Membresía (`membresias`)
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | PK auto |
| nombre | String(100) | Ej: "Membresía Mensual" |
| descripcion | TEXT | Beneficios |
| duracionDias | Integer | 7, 30, 90, 180, 365 |
| precio | BigDecimal | En soles (PEN) |
| estado | Boolean | Activa para venta |
| color | String(20) | Color en frontend |
| orden | Integer | Orden visual |

### Pago (`pagos`)
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | PK |
| cliente_id | FK→Cliente | |
| membresia_id | FK→Membresia | |
| planNombre | String(80) | "Mensual", "3 Meses" |
| planDias | Integer | 30, 90, etc. |
| montoFinal | BigDecimal | Monto cobrado |
| estado | String(20) | "pendiente"/"aprobado"/"rechazado"/"reembolsado" |
| metodo_pago | String(30) | "Efectivo"/"Tarjeta"/"MercadoPago"/"Yape"/"Manual" |
| mp_preference_id | String(64) | ID de MercadoPago |
| mp_payment_id | String(64) | Payment ID |
| fecha_registro | Instant | Auto |

### Asistencia (`asistencias`)
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | PK |
| cliente_id | FK→Cliente | |
| fecha_hora | LocalDateTime | Momento del registro |
| tipo_registro | Enum | QR_AUTO, MANUAL, WEB |
| latitud/longitud | Double | Geolocalización |
| dispositivo | TEXT | User-Agent |
| ip_address | String(45) | IP |

**⚠️ NO tiene campo de hora de salida — el cliente lo solicitó**

### Otras entidades
- **Descuento** → Porcentajes aplicables a membresías
- **Renovacion** → Historial de renovaciones (tracking)
- **HistorialAcceso** → Log de accesos de usuarios del sistema
- **HistorialPagoFallido** → Log de pagos rechazados (HU-33)
- **RegistroNotificacion** → Anti-spam para emails de vencimiento (HU-31)
- **ConfiguracionNotificacion** → Config de email/SMS
- **PreferenciaMP** → Preferencias de MercadoPago
- **TokenInvalido** → Blacklist de JWT tokens

---

## 🌐 Controladores y Endpoints (114 mappings)

### Públicos (sin JWT)
- `POST /api/auth/login` → Login
- `POST /api/auth/refresh` → Refresh token
- `POST /api/pagos/webhook` → Webhook MercadoPago

### Dashboard (`/api/dashboard/`)
- `GET /stats` → KPIs principales
- `GET /ingresos-semana` → Ingresos últimos 7 días
- `GET /asistencias-por-hora` → Horas pico
- `GET /actividad-reciente` → Timeline de actividad
- `GET /asistencias-tendencia` → Tendencia 7/30 días (HU-26)
- `GET /top-clientes` → Top por asistencias (HU-26)

### Reportes (`/api/reportes/`)
- Métricas comparativas, suscripciones, ingresos por método/plan, historial pagos, retención, exports Excel

### Recepción (`/api/recepcion/`)
- `GET /por-vencer` → Bandeja con prioridad (HU-32)
- `PATCH /clientes/{id}/seguimiento` → Actualizar estado (HU-32)

### Clientes (`/api/clientes/`)
- CRUD completo + `/inactividad` (HU-34) + `/exportar-inactivos` (HU-35)

### Otros
- `/api/asistencias/` → Registro QR, manual, historial
- `/api/pagos/` → Crear preferencia, webhook, Yape, reembolsos
- `/api/membresias/` → CRUD
- `/api/usuarios/` → CRUD + perfil
- `/api/roles/` → CRUD
- `/api/permisos/` → Listado
- `/api/descuentos/` → CRUD
- `/api/configuracion/notificaciones/` → Config email/SMS

---

## 🔧 Integraciones Externas

1. **MercadoPago** → Pagos online (tarjeta, QR), reembolsos, webhooks
2. **Twilio** → SMS y WhatsApp (opcional, puede estar desconfigurado)
3. **Gmail SMTP** → Emails de notificación (vencimiento, pago fallido)
4. **ZXing** → Generación de códigos QR (PNG/Base64)
5. **Apache POI** → Exportación a Excel (.xlsx)

---

## ⏰ Procesos Automáticos

- **VencimientoNotificacionScheduler** → Cron diario 6:00 AM
  - Busca clientes con vencimiento en 30, 15, 7, 3, 1 día(s)
  - Envía email HTML personalizado
  - Registra en `registro_notificaciones` para evitar duplicados

---

## 📝 Convenciones del Proyecto

- **Lombok:** @Data, @Builder, @RequiredArgsConstructor en todo
- **DTOs:** Sufijos `Request`/`Response`/`DTO`
- **Logs:** Emojis descriptivos (✅, ❌, ⚠️, 📧, 💾, 🔄)
- **Fechas:** LocalDate para fechas, LocalDateTime para timestamps, Instant para auditoría
- **ddl-auto:** `update` (Hibernate crea/modifica tablas automáticamente)
- **Seguridad de endpoints:** `@PreAuthorize("hasAnyRole('ADMIN', 'RECEPCIONISTA')")`

---

## 🗂️ HUs Implementadas (Sprint Backlog 3)

| HU | Estado | Descripción |
|----|--------|-------------|
| HU-26 | ✅ | Métricas de asistencia (tendencia, top clientes) |
| HU-27 | ✅ | Dashboard KPIs |
| HU-28 | ✅ | Reportes de suscripciones (filtros, paginación, export) |
| HU-29 | ✅ | Reportes de ingresos (por método, por plan) |
| HU-30 | ✅ | Historial pagos + Retención mensual + Export Excel |
| HU-31 | ✅ | Recordatorios automáticos de vencimiento (cron + email) |
| HU-32 | ✅ | Bandeja de recepción (por vencer + estado seguimiento) |
| HU-33 | ✅ | Alerta automática pago rechazado (email + historial) |
| HU-34 | ✅ | Detección inactividad (15/30/60d, badges colores) |
| HU-35 | ✅ | Exportar inactivos Excel (campañas marketing) |

---

## 🧪 Comandos Útiles

```powershell
# Compilar
.\mvnw.cmd compile -q

# Ejecutar
.\mvnw.cmd spring-boot:run

# Ver qué usa el puerto 8080
Get-NetTCPConnection -LocalPort 8080

# Matar proceso en puerto 8080
Get-NetTCPConnection -LocalPort 8080 | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
```

---

## 📋 Documentación Existente

- `FRONTEND_INTEGRACION.md` → Guía completa de endpoints para frontend (13 endpoints nuevos)
- `backend_requirements.md` → Requisitos del frontend para HU-30/31
