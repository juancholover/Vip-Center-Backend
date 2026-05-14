# 📋 Guía de Integración Frontend — Sprint Backlog 3 (HU-26 a HU-31)

> **Fecha:** 12/05/2026  
> **Backend Base URL:** `http://localhost:8080`  
> **Autenticación:** Todas las rutas requieren `Authorization: Bearer <JWT>` (excepto las marcadas como públicas)  
> **Content-Type:** `application/json`

---

## 📌 Índice

- [HU-26: Métricas de Asistencias (Dashboard)](#hu-26-métricas-de-asistencias-dashboard)
- [HU-27: Dashboard General y KPIs](#hu-27-dashboard-general-y-kpis)
- [HU-28: Reportes de Suscripciones](#hu-28-reportes-de-suscripciones)
- [HU-29: Reportes de Ingresos](#hu-29-reportes-de-ingresos)
- [HU-30: Historial de Pagos y Retención](#hu-30-historial-de-pagos-y-retención)
- [HU-31: Recordatorios Automáticos](#hu-31-recordatorios-automáticos-de-vencimiento)

---

## HU-26: Métricas de Asistencias (Dashboard)

### 1. Tendencia de Asistencias

```
GET /api/dashboard/asistencias-tendencia?dias=7
```

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `dias` | Integer | No | `7` | Cantidad de días hacia atrás (7 o 30) |

**Respuesta:**
```json
[
  { "fecha": "lun.", "cantidad": 45 },
  { "fecha": "mar.", "cantidad": 52 },
  { "fecha": "mié.", "cantidad": 38 },
  { "fecha": "jue.", "cantidad": 60 },
  { "fecha": "vie.", "cantidad": 73 },
  { "fecha": "sáb.", "cantidad": 41 },
  { "fecha": "dom.", "cantidad": 15 }
]
```

### 2. Top Clientes (Más Asistencias)

```
GET /api/dashboard/top-clientes?limite=10
```

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `limite` | Integer | No | `10` | Máximo de clientes a retornar |

**Respuesta:**
```json
[
  { "id": 1, "nombre": "Juan Pérez García", "asistencias": 22 },
  { "id": 5, "nombre": "María López Torres", "asistencias": 20 },
  { "id": 3, "nombre": "Carlos Rodríguez", "asistencias": 18 }
]
```

### 3. Asistencias por Hora (ya existente)

```
GET /api/dashboard/asistencias-por-hora
```

**Respuesta:**
```json
[
  { "hora": "06:00", "intensidad": 12 },
  { "hora": "07:00", "intensidad": 25 },
  { "hora": "18:00", "intensidad": 48 }
]
```

---

## HU-27: Dashboard General y KPIs

### 4. Estadísticas del Dashboard (ya existente)

```
GET /api/dashboard/stats
```

**Respuesta:**
```json
{
  "clientesActivos": 145,
  "ingresosMes": 45280.50,
  "asistenciasHoy": 67,
  "membresiasPorVencer": 12,
  "promedioDiario": 1509.35
}
```

### 5. Ingresos de la Semana (ya existente)

```
GET /api/dashboard/ingresos-semana
```

**Respuesta:**
```json
[
  { "dia": "Lun", "monto": 1250.00 },
  { "dia": "Mar", "monto": 980.50 },
  { "dia": "Mié", "monto": 2100.00 }
]
```

### 6. Actividad Reciente (ya existente)

```
GET /api/dashboard/actividad-reciente
```

**Respuesta:**
```json
[
  {
    "tipo": "ASISTENCIA",
    "descripcion": "Juan Pérez registró asistencia",
    "tiempoRelativo": "Hace 5 minutos",
    "icono": "check-circle"
  }
]
```

### 7. Métricas Comparativas con Rango

```
GET /api/reportes/metricas-comparativas?inicio=2026-05-01&fin=2026-05-12
```

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `inicio` | LocalDate | No | Primer día del mes | Fecha inicio (YYYY-MM-DD) |
| `fin` | LocalDate | No | Hoy | Fecha fin (YYYY-MM-DD) |

**Respuesta:**
```json
[
  {
    "nombre": "Total Asistencias",
    "valorActual": "345",
    "valorAnterior": "310",
    "porcentajeCambio": 11.29,
    "tendencia": "up",
    "icono": "users",
    "categoria": "asistencia"
  },
  {
    "nombre": "Ingresos Totales",
    "valorActual": "$45280.00",
    "valorAnterior": "$38500.00",
    "porcentajeCambio": 17.61,
    "tendencia": "up",
    "icono": "dollar-sign",
    "categoria": "ingreso"
  }
]
```

> **Nota:** `tendencia` puede ser `"up"`, `"down"` o `"neutral"`.  
> **Nota:** `categoria` puede ser `"asistencia"`, `"suscripcion"` o `"ingreso"`.

---

## HU-28: Reportes de Suscripciones

### 8. Listado Paginado de Suscripciones (NUEVO)

```
GET /api/reportes/suscripciones?estado=activa&page=0&size=10
```

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `estado` | String | No | todos | `"activa"`, `"vencida"`, `"por_vencer"`, `"sin_membresia"` |
| `fechaInicio` | LocalDate | No | - | Inicio rango vencimiento (YYYY-MM-DD) |
| `fechaFin` | LocalDate | No | - | Fin rango vencimiento (YYYY-MM-DD) |
| `diasAnticipacion` | Integer | No | `15` | Para "por_vencer": días de anticipación |
| `page` | Integer | No | `0` | Número de página |
| `size` | Integer | No | `10` | Tamaño de página |

**Respuesta (paginada):**
```json
{
  "content": [
    {
      "clienteId": 1,
      "nombreCompleto": "Juan Pérez García",
      "email": "juan@email.com",
      "telefono": "987654321",
      "plan": "Plan Mensual",
      "fechaVencimiento": "2026-05-20",
      "estado": "ACTIVA"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalPages": 5,
  "totalElements": 48,
  "first": true,
  "last": false,
  "numberOfElements": 10,
  "empty": false
}
```

> **Estados posibles:** `"ACTIVA"`, `"VENCIDA"`, `"POR_VENCER"`, `"SIN_MEMBRESIA"`, `"QR_DESHABILITADO"`

### 9. Exportar Suscripciones a Excel (NUEVO)

```
GET /api/reportes/suscripciones/exportar?estado=activa
```

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `estado` | String | No | todos | Mismos filtros que el endpoint paginado |
| `fechaInicio` | LocalDate | No | - | Inicio rango |
| `fechaFin` | LocalDate | No | - | Fin rango |
| `diasAnticipacion` | Integer | No | `15` | Para "por_vencer" |

**Respuesta:** Archivo `.xlsx` (binary)

**Ejemplo de consumo en frontend:**
```javascript
const response = await fetch('/api/reportes/suscripciones/exportar?estado=activa', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const blob = await response.blob();
const url = window.URL.createObjectURL(blob);
const a = document.createElement('a');
a.href = url;
a.download = 'reporte_suscripciones.xlsx';
a.click();
```

---

## HU-29: Reportes de Ingresos

### 10. Ingresos por Método de Pago (NUEVO)

```
GET /api/reportes/ingresos/por-metodo?fechaInicio=2026-05-01&fechaFin=2026-05-12
```

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `fechaInicio` | LocalDate | No | Primer día del mes | Fecha inicio (YYYY-MM-DD) |
| `fechaFin` | LocalDate | No | Hoy | Fecha fin (YYYY-MM-DD) |

**Respuesta:**
```json
[
  { "metodo": "Tarjeta", "total": 28500.00, "cantidad": 42, "porcentaje": 62.35 },
  { "metodo": "Yape/Plin", "total": 12300.00, "cantidad": 35, "porcentaje": 26.90 },
  { "metodo": "Efectivo", "total": 4920.50, "cantidad": 18, "porcentaje": 10.76 }
]
```

### 11. Ingresos por Plan de Membresía (NUEVO)

```
GET /api/reportes/ingresos/por-plan?fechaInicio=2026-05-01&fechaFin=2026-05-12
```

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `fechaInicio` | LocalDate | No | Primer día del mes | Fecha inicio (YYYY-MM-DD) |
| `fechaFin` | LocalDate | No | Hoy | Fecha fin (YYYY-MM-DD) |

**Respuesta:**
```json
[
  { "plan": "Plan Anual", "total": 24000.00, "cantidad": 8, "porcentaje": 52.47 },
  { "plan": "Plan Mensual", "total": 15200.00, "cantidad": 38, "porcentaje": 33.24 },
  { "plan": "Plan Semanal", "total": 6540.00, "cantidad": 49, "porcentaje": 14.30 }
]
```

---

## HU-30: Historial de Pagos y Retención

### 12. Historial de Pagos con Buscador (NUEVO)

```
GET /api/reportes/pagos/historial?busqueda=Juan
```

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `busqueda` | String | No | `""` | Buscar por nombre, apellido o teléfono |

**Respuesta:**
```json
[
  {
    "pagoId": 152,
    "fecha": "2026-05-10",
    "hora": "14:30",
    "cliente": "Juan Pérez García",
    "plan": "Plan Mensual",
    "metodo": "Tarjeta",
    "monto": 150.00,
    "estado": "Aprobado"
  },
  {
    "pagoId": 148,
    "fecha": "2026-05-08",
    "hora": "09:15",
    "cliente": "Juan López Torres",
    "plan": "Plan Anual",
    "metodo": "Yape/Plin",
    "monto": 1200.00,
    "estado": "Aprobado"
  }
]
```

> **Estados posibles:** `"Aprobado"`, `"Pendiente"`, `"Rechazado"`

### 13. Retención Mensual (NUEVO)

```
GET /api/reportes/retencion
```

**Sin parámetros.** Devuelve últimos 12 meses automáticamente.

**Respuesta:**
```json
[
  { "anio": 2025, "mesNumero": 6, "mes": "Junio", "renovaciones": 45, "cancelaciones": 8 },
  { "anio": 2025, "mesNumero": 7, "mes": "Julio", "renovaciones": 52, "cancelaciones": 5 },
  { "anio": 2025, "mesNumero": 8, "mes": "Agosto", "renovaciones": 38, "cancelaciones": 12 }
]
```

> **Cálculo tasa de retención en frontend:**
> ```javascript
> const tasa = (renovaciones / (renovaciones + cancelaciones)) * 100;
> ```

### 14. Exportar Historial de Pagos a Excel (NUEVO)

```
GET /api/reportes/pagos/historial/exportar?busqueda=
```

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `busqueda` | String | No | `""` | Mismo filtro que historial |

**Respuesta:** Archivo `.xlsx` (binary)

**Ejemplo de consumo:**
```javascript
const response = await fetch('/api/reportes/pagos/historial/exportar', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const blob = await response.blob();
const url = window.URL.createObjectURL(blob);
const a = document.createElement('a');
a.href = url;
a.download = 'historial_pagos.xlsx';
a.click();
```

---

## HU-31: Recordatorios Automáticos de Vencimiento

> **⚠️ IMPORTANTE:** Este módulo es 100% backend (Cron Job). No requiere endpoints para el frontend.

### Funcionamiento

1. **Cron Job** se ejecuta automáticamente **cada día a las 6:00 AM**
2. Busca clientes cuya membresía vence en exactamente **30, 15, 7, 3 o 1 día(s)**
3. Envía un **email HTML** con diseño profesional al cliente
4. Registra el envío en tabla `registro_notificaciones` para **evitar duplicados**
5. Si el cliente renueva (nueva fecha de vencimiento), se permite re-enviar

### Datos que registra (tabla `registro_notificaciones`)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | Long | Auto-generado |
| `cliente_id` | Long (FK) | Referencia al cliente |
| `tipo` | String | `"EMAIL"` |
| `dias_antes` | Integer | 30, 15, 7, 3 o 1 |
| `fecha_vencimiento_referencia` | LocalDate | Fecha de vencimiento del cliente |
| `fecha_envio` | LocalDateTime | Cuándo se envió |
| `exitoso` | Boolean | Si se envió correctamente |
| `mensaje_error` | String | Error si falló |

### Configuración

La configuración de email se controla desde:
1. **Base de datos:** tabla `configuracion_notificacion` (campo `email_enabled`)
2. **Properties fallback:** `app.email.enabled=true` en `application.properties`

Si `email_enabled = false`, el scheduler no enviará nada.

---

## 📊 Resumen de Endpoints

### Nuevos Endpoints (7)

| # | Método | URL | HU | Descripción |
|---|--------|-----|----|-------------|
| 1 | GET | `/api/dashboard/asistencias-tendencia` | HU-26 | Tendencia de asistencias (7/30 días) |
| 2 | GET | `/api/dashboard/top-clientes` | HU-26 | Top clientes por asistencias |
| 3 | GET | `/api/reportes/suscripciones` | HU-28 | Listado paginado con filtros |
| 4 | GET | `/api/reportes/suscripciones/exportar` | HU-28 | Exportar a Excel |
| 5 | GET | `/api/reportes/ingresos/por-metodo` | HU-29 | Ingresos por método de pago |
| 6 | GET | `/api/reportes/ingresos/por-plan` | HU-29 | Ingresos por plan |
| 7 | GET | `/api/reportes/pagos/historial` | HU-30 | Historial con buscador |
| 8 | GET | `/api/reportes/retencion` | HU-30 | Retención mensual |
| 9 | GET | `/api/reportes/pagos/historial/exportar` | HU-30 | Exportar historial a Excel |

### Endpoints Ya Existentes (útiles)

| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/api/dashboard/stats` | Estadísticas generales |
| GET | `/api/dashboard/ingresos-semana` | Ingresos últimos 7 días |
| GET | `/api/dashboard/asistencias-por-hora` | Horas pico |
| GET | `/api/dashboard/actividad-reciente` | Actividad reciente |
| GET | `/api/reportes/metricas-comparativas` | Métricas con comparación |
| GET | `/api/reportes/ingresos/metodos-pago` | Distribución métodos pago |
| GET | `/api/reportes/ingresos/distribucion-plan` | Distribución por plan |
| GET | `/api/reportes/suscripciones/renovaciones-cancelaciones` | Histórico renovaciones |
| GET | `/api/reportes/suscripciones/proximos-vencer` | Próximos a vencer |
| GET | `/api/reportes/suscripciones/distribucion-estado` | Distribución por estado |
| GET | `/api/reportes/suscripciones/distribucion-membresia` | Distribución por membresía |

---

## HU-32: Bandeja de Clientes por Vencer (Recepción)

### 15. Listado de Clientes por Vencer con Prioridad (NUEVO)

```
GET /api/recepcion/por-vencer?dias=15
```

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `dias` | Integer | No | `15` | Días de anticipación |

**Respuesta (ordenada por días restantes ASC — los de 1 día primero):**
```json
[
  {
    "clienteId": 12,
    "nombreCompleto": "Carlos Mendoza Ríos",
    "telefono": "987654321",
    "email": "carlos@email.com",
    "plan": "Plan Mensual",
    "fechaVencimiento": "2026-05-13",
    "diasRestantes": 1,
    "estadoSeguimiento": "PENDIENTE",
    "avatar": "CM"
  },
  {
    "clienteId": 5,
    "nombreCompleto": "Ana Torres López",
    "telefono": "912345678",
    "email": "ana@email.com",
    "plan": "Plan Anual",
    "fechaVencimiento": "2026-05-19",
    "diasRestantes": 7,
    "estadoSeguimiento": "LLAMADO",
    "avatar": "AT"
  }
]
```

> **Estados de seguimiento:** `"PENDIENTE"`, `"LLAMADO"`, `"PROMESA"`

### 16. Actualizar Estado de Seguimiento (NUEVO)

```
PATCH /api/recepcion/clientes/{id}/seguimiento
```

**Body:**
```json
{
  "estadoSeguimiento": "LLAMADO"
}
```

| Campo | Tipo | Requerido | Valores Válidos |
|-------|------|-----------|-----------------|
| `estadoSeguimiento` | String | Sí | `"PENDIENTE"`, `"LLAMADO"`, `"PROMESA"` |

**Respuesta éxito (200):**
```json
{
  "clienteId": 12,
  "estadoAnterior": "PENDIENTE",
  "estadoNuevo": "LLAMADO",
  "mensaje": "Estado de seguimiento actualizado correctamente"
}
```

**Respuesta error (400):**
```json
{
  "error": "Estado inválido",
  "mensaje": "Los estados válidos son: PENDIENTE, LLAMADO, PROMESA",
  "estadoRecibido": "INVALIDO"
}
```

**Ejemplo de uso en frontend (botones de acción por fila):**
```javascript
async function marcarComoLlamado(clienteId) {
  const response = await fetch(`/api/recepcion/clientes/${clienteId}/seguimiento`, {
    method: 'PATCH',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ estadoSeguimiento: 'LLAMADO' })
  });
  const result = await response.json();
  console.log(result.mensaje);
}
```

---

## HU-33: Alerta Automática de Pago Rechazado/Pendiente

> **⚠️ IMPORTANTE:** Este módulo es mayormente backend automático. El frontend solo necesita consumir el historial de pagos fallidos.

### Funcionamiento Automático

1. Cuando un **pago Yape es rechazado** o **un webhook de MercadoPago reporta un pago no aprobado**:
   - Se registra automáticamente en la tabla `historial_pagos_fallidos`
   - Se envía un **email HTML de alerta** al cliente de forma asíncrona
   - El email explica el problema e instruye al cliente a actualizar su método de pago

2. **El administrador puede ver** los pagos fallidos en el historial existente:
   - Los pagos rechazados aparecen con estado `"Rechazado"` en `GET /api/reportes/pagos/historial`

### Datos que registra (tabla `historial_pagos_fallidos`)

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | Long | Auto-generado |
| `cliente_id` | Long (FK) | Cliente afectado |
| `pago_id` | Long (FK) | Pago que falló |
| `estado_pago` | String | `"rejected"`, `"cancelled"`, `"pending"` |
| `motivo_detalle` | String | Motivo traducido (ej: "Saldo insuficiente") |
| `monto_intentado` | BigDecimal | Monto que se intentó cobrar |
| `metodo_pago` | String | "Yape", "MercadoPago", etc. |
| `notificacion_enviada` | Boolean | Si el email de alerta fue enviado |
| `fecha_registro` | Instant | Cuándo ocurrió el fallo |

### Plantilla del Email de Alerta

El email incluye:
- **Header rojo** indicando problema con el pago
- **Estado y motivo** del rechazo (ej: "Saldo insuficiente")
- **Detalles del pago** (plan, monto)
- **Instrucciones** para el cliente (verificar fondos, actualizar método, visitar recepción)
- **Botón CTA** "Reintentar Pago"

---

## 📊 Resumen de Endpoints Completo

### Nuevos Endpoints Sprint 3 (11 total)

| # | Método | URL | HU | Descripción |
|---|--------|-----|----|-------------|
| 1 | GET | `/api/dashboard/asistencias-tendencia` | HU-26 | Tendencia de asistencias (7/30 días) |
| 2 | GET | `/api/dashboard/top-clientes` | HU-26 | Top clientes por asistencias |
---

## HU-34: Detectar Clientes sin Asistencia por Periodos

### 17. Panel de Alertas de Inactividad (NUEVO)

```
GET /api/clientes/inactividad?diasMinimo=0&nivelRiesgo=
```

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `diasMinimo` | Integer | No | `0` | Mínimo de días de inactividad (usar 15 para omitir activos recientes) |
| `nivelRiesgo` | String | No | todos | Filtrar por nivel: `"BAJO"`, `"MEDIO"`, `"ALTO"`, `"CRITICO"` |

**Respuesta:**
```json
{
  "totalInactivos": 42,
  "bajo": 15,
  "medio": 12,
  "alto": 9,
  "critico": 6,
  "clientes": [
    {
      "clienteId": 8,
      "nombreCompleto": "Carlos Mendoza Ríos",
      "telefono": "987654321",
      "email": "carlos@email.com",
      "plan": "Plan Mensual",
      "fechaVencimiento": "2026-06-15",
      "ultimaAsistencia": "2026-02-10T14:30:00",
      "diasInactivo": 91,
      "nivelRiesgo": "CRITICO",
      "colorBadge": "red"
    },
    {
      "clienteId": 15,
      "nombreCompleto": "Ana Torres López",
      "telefono": "912345678",
      "email": "ana@email.com",
      "plan": "Plan Anual",
      "fechaVencimiento": "2026-12-01",
      "ultimaAsistencia": "2026-04-20T09:15:00",
      "diasInactivo": 22,
      "nivelRiesgo": "MEDIO",
      "colorBadge": "yellow"
    }
  ]
}
```

> **Niveles de riesgo y colores para badges:**
> | Nivel | Días | Color | Badge CSS |
> |-------|------|-------|-----------|
> | `BAJO` | < 15 | `green` | `background: #22c55e` |
> | `MEDIO` | 15-30 | `yellow` | `background: #eab308` |
> | `ALTO` | 30-60 | `orange` | `background: #f97316` |
> | `CRITICO` | > 60 | `red` | `background: #ef4444` |

> **Nota:** Si `ultimaAsistencia` es `null`, el cliente nunca ha asistido (999 días inactivo, nivel CRITICO).

---

## HU-35: Exportar Listas de Clientes Inactivos (Campañas)

### 18. Exportar Base de Inactivos a Excel (NUEVO)

```
GET /api/clientes/exportar-inactivos?diasMinimo=15
```

| Parámetro | Tipo | Requerido | Default | Descripción |
|-----------|------|-----------|---------|-------------|
| `diasMinimo` | Integer | No | `15` | Mínimo de días para incluir en exportación |

**Respuesta:** Archivo `.xlsx` (binary) con 2 hojas:

**Hoja 1 — "Clientes Inactivos":**
| ID | Nombre Completo | Teléfono | Email | Plan | Fecha Vencimiento | Última Asistencia | Días Inactivo | Nivel de Riesgo |
|----|-----------------|----------|-------|------|-------------------|-------------------|---------------|-----------------|

**Hoja 2 — "Resumen":**
| Categoría | Cantidad |
|-----------|----------|
| Total Inactivos | 42 |
| Bajo Riesgo (<15 días) | 15 |
| Medio Riesgo (15-30 días) | 12 |
| Alto Riesgo (30-60 días) | 9 |
| Crítico (>60 días) | 6 |

> ⚠️ **Solo ADMIN puede acceder a este endpoint** — contiene datos sensibles (teléfono, email).

**Ejemplo de consumo:**
```javascript
async function exportarInactivos() {
  const response = await fetch('/api/clientes/exportar-inactivos?diasMinimo=15', {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'clientes_inactivos.xlsx';
  a.click();
}
```

---

## 📊 Resumen de Endpoints Completo

### Nuevos Endpoints Sprint 3 (13 total)

| # | Método | URL | HU | Descripción |
|---|--------|-----|----|-------------|
| 1 | GET | `/api/dashboard/asistencias-tendencia` | HU-26 | Tendencia de asistencias (7/30 días) |
| 2 | GET | `/api/dashboard/top-clientes` | HU-26 | Top clientes por asistencias |
| 3 | GET | `/api/reportes/suscripciones` | HU-28 | Listado paginado con filtros |
| 4 | GET | `/api/reportes/suscripciones/exportar` | HU-28 | Exportar a Excel |
| 5 | GET | `/api/reportes/ingresos/por-metodo` | HU-29 | Ingresos por método de pago |
| 6 | GET | `/api/reportes/ingresos/por-plan` | HU-29 | Ingresos por plan |
| 7 | GET | `/api/reportes/pagos/historial` | HU-30 | Historial con buscador |
| 8 | GET | `/api/reportes/retencion` | HU-30 | Retención mensual |
| 9 | GET | `/api/reportes/pagos/historial/exportar` | HU-30 | Exportar historial a Excel |
| 10 | GET | `/api/recepcion/por-vencer` | HU-32 | Bandeja clientes por vencer |
| 11 | PATCH | `/api/recepcion/clientes/{id}/seguimiento` | HU-32 | Actualizar estado seguimiento |
| 12 | GET | `/api/clientes/inactividad` | HU-34 | Panel alertas de inactividad |
| 13 | GET | `/api/clientes/exportar-inactivos` | HU-35 | Exportar inactivos a Excel (ADMIN) |

### Módulos Backend Automáticos (sin endpoints frontend)

| HU | Descripción |
|----|-------------|
| HU-31 | Cron Job diario 6AM — Emails de recordatorio de vencimiento (30, 15, 7, 3, 1 días) |
| HU-33 | Alerta automática por email cuando un pago Yape/MercadoPago es rechazado o cancelado |

---

## 🔐 Seguridad

- Todos los endpoints requieren autenticación JWT
- Roles permitidos: `ADMIN` y `RECEPCIONISTA` (excepto los marcados)
- **Solo ADMIN:** `/api/clientes/exportar-inactivos` (HU-35), `/api/reportes/membresias/mas-vendidas`
- El token se envía como: `Authorization: Bearer <token>`
- Los endpoints de exportación Excel devuelven `204 No Content` si no hay datos

## 📅 Formato de Fechas

- **Parámetros de entrada:** `YYYY-MM-DD` (ej: `2026-05-12`)
- **Respuesta fechas:** `YYYY-MM-DD` o `dd/MM/yyyy` según el endpoint
- **Zona horaria:** Los cálculos usan la zona del servidor (configurada como UTC en BD, convertida a America/Lima en servicio)
