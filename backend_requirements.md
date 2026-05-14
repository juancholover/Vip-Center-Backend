## Requerimientos Backend - Sprint Backlog 3 (HU-26 y HU-27)

Para poder integrar completamente las historias de usuario de Dashboard (HU-26 y HU-27) en el Frontend, es necesario implementar los siguientes endpoints y sus estructuras en el Controlador de Dashboard (`DashboardController`).

### 1. HU-26: Métricas de Asistencias

Se necesitan 3 endpoints principales para métricas estadísticas:

**A. `/api/dashboard/asistencias-tendencia`**
- **Descripción:** Devuelve la cantidad de asistencias totales agrupadas por fecha (últimos 7 o 30 días) para armar una tendencia.
- **Respuesta Esperada:** Un array de objetos con `fecha` (ej. 'Lun', '01/05') y `cantidad`.
```json
[
  { "fecha": "Lun", "cantidad": 45 },
  { "fecha": "Mar", "cantidad": 52 }
]
```

**B. `/api/dashboard/asistencias-por-hora` (horas-pico)**
- **Descripción:** Devuelve la cuenta total de asistencias agrupadas por hora del día para poder graficar horas pico.
- **Respuesta Esperada:** Un array de objetos con `hora` y `cantidad`.
```json
[
  { "hora": "06:00", "cantidad": 8 },
  { "hora": "08:00", "cantidad": 15 }
]
```

**C. `/api/dashboard/top-clientes`**
- **Descripción:** Devuelve el ranking (top 5 o 10) de los clientes con más asistencias registradas en el periodo actual.
- **Respuesta Esperada:** Un array de objetos con el id del cliente, nombre y cantidad de asistencias.
```json
[
  { "id": 1, "nombre": "Juan Pérez", "asistencias": 22 },
  { "id": 2, "nombre": "María García", "asistencias": 20 }
]
```

### 2. HU-27: Dashboard general y KPIs

Se requiere centralizar las métricas agrupadas.

**A. `/api/dashboard/stats` (o `/kpi`)**
- **Descripción:** Retorna el DTO `DashboardStatsDTO` para evitar múltiples llamadas en la cabecera principal del dashboard.
- **Respuesta Esperada:**
```json
{
  "clientesActivos": 156,
  "ingresosMes": 12450.0,
  "asistenciasHoy": 42,
  "membresiasPorVencer": 8
}
```

### 3. HU-028: Reporte de Suscripciones

Se necesita un endpoint paginado y filtrado, junto a su versión de exportación a Excel.

**A. `/api/reportes/suscripciones`**
- **Descripción:** Devuelve un listado paginado de suscripciones con base en el estado (activa, vencida, por_vencer), fechaInicio y fechaFin. Se requiere usar Specifications de JPA o QueryDSL.
- **Respuesta Esperada:** Un objeto Page (formato típico de Spring Data) que contenta el contenido en `content` y los metadatos de paginación (`totalPages`, `totalElements`).
```json
{
  "content": [
    {
      "id": 1,
      "cliente": "Juan Pérez",
      "plan": "Anual",
      "fechaInicio": "2024-01-10",
      "fechaFin": "2025-01-10",
      "estado": "activa"
    }
  ],
  "totalPages": 1,
  "totalElements": 1
}
```

**B. `/api/reportes/suscripciones/exportar`**
- **Descripción:** Endpoint que acepta los mismos parámetros que el anterior (estado, fechaInicio, fechaFin) pero en lugar de retornar JSON, usa Apache POI para devolver el archivo Excel adjunto como tipo `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`.

### 4. HU-029: Reportes de Ingresos (Por Método y Plan)

Para los gráficos de Torta/Dona requerimos ingresos segmentados de acuerdo a fechas (opcionales para el filtrado dinámico).

**A. `/api/reportes/ingresos/por-metodo`**
- **Descripción:** Devuelve la sumatoria de ingresos consolidados agrupados por método de pago.
- **Respuesta Esperada:**
```json
[
  { "metodo": "Tarjeta", "total": 12000, "cantidad": 80, "porcentaje": 35 },
  { "metodo": "Yape/Plin", "total": 15400, "cantidad": 120, "porcentaje": 45 }
]
```

**B. `/api/reportes/ingresos/por-plan`**
- **Descripción:** Devuelve la sumatoria de ingresos consolidados agrupados por plan de membresía.
- **Respuesta Esperada:**
```json
[
  { "plan": "Plan Anual", "total": 20000, "cantidad": 40, "porcentaje": 60 },
  { "plan": "Plan Mensual", "total": 10000, "cantidad": 100, "porcentaje": 30 }
]
```

### 5. HU-030: Generar reportes de pagos y renovaciones (historial, retención)

**A. Cálculo de Retención y Endpoint**
- **Descripción:** Lógica matemática en backend para calcular la tasa de retención mensual (fórmula: `(Renovaciones / (Renovaciones + Cancelaciones)) * 100`) o similar según negocio.
- **Endpoint 1 (`/api/reportes/pagos/historial`):** Devuelve historial detallado.
```json
[
  {
    "pagoId": 1,
    "fecha": "2026-05-12",
    "hora": "10:30",
    "cliente": "Juan Pérez",
    "plan": "Anual",
    "metodo": "Tarjeta",
    "monto": 1200,
    "estado": "Aprobado"
  }
]
```
- **Endpoint 2 (`/api/reportes/retencion`):**
```json
[
  { "anio": 2026, "mesNumero": 5, "mes": "Mayo", "renovaciones": 45, "cancelaciones": 5 }
]
```

**B. Exportación (`/api/reportes/pagos/historial/exportar`)**
- Extraer lo mismo pero usando POI para archivo Excel (Blob).

### 6. HU-031: Enviar recordatorios automáticos de vencimiento (30, 15, 7 y 1 día antes)

*(Historia enfocada exclusivamente en el Backend)*

- **Tabla:** Crear tabla `registro_notificaciones` para evitar envíos duplicados/spam.
- **Cron Job:** Desarrollar tarea `@Scheduled` (ej. ejecutada cada madrugada) para buscar suscripciones que venzan en exactamente 30, 15, 7 o 1 día(s).
- **Notificación:** Configurar conexión con servidor SMTP (Spring Mail) o SMS (Twilio).
- **Plantillas:** Diseñar plantillas HTML/Thymeleaf para inyectar datos del usuario y fecha exacta.
- **Post-envío:** Lógica para insertar el registro en `registro_notificaciones` tras cada envío exitoso.
