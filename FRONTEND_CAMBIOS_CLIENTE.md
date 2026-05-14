# 📋 Cambios Solicitados por el Cliente — Para Frontend

> **Fecha:** 13/05/2026  
> **Reunión con:** David Condor (dueño del gimnasio VIP Center Fit) + su asistente  
> **Resultado:** El cliente aprobó el sistema general pero solicitó ajustes puntuales

---

## 1. 🆔 Nuevo Campo: DNI

**Qué dijo el cliente:**
> "Podrías poner DNI, por ejemplo. Hay personas que les gusta que le hagamos boletas con su DNI."

**Cambio en API:**
- El endpoint `POST /api/clientes` y `PUT /api/clientes/{id}` ahora aceptarán un campo `dni` (String, opcional, 8-20 caracteres)
- El endpoint `GET /api/clientes` y `GET /api/clientes/{id}` incluirán `dni` en la respuesta

**Ejemplo de body actualizado (registro de cliente):**
```json
{
  "nombre": "Carlos",
  "apellido": "Mendoza",
  "telefono": "987654321",
  "email": "carlos@email.com",
  "dni": "74521389"
}
```

**Ejemplo de respuesta actualizada:**
```json
{
  "id": 1,
  "nombre": "Carlos",
  "apellido": "Mendoza",
  "nombreCompleto": "Carlos Mendoza",
  "telefono": "987654321",
  "email": "carlos@email.com",
  "dni": "74521389",
  "estado": "activo",
  ...
}
```

**En el frontend:**
1. Agregar campo "DNI" en el formulario de registro de cliente (después de teléfono, antes de email)
2. Agregar columna "DNI" en la tabla de clientes
3. El campo es **opcional** — no es obligatorio para registrar

---

## 2. 🏷️ Cambio de Etiqueta: "sin_membresia" → "vencido"

**Qué dijo el cliente:**
> "En vez de 'sin membresía' debería decir 'vencido'"

**Cambio en API:**
- El campo `estado` en `ClienteResponse` ya **NO** devolverá `"sin_membresia"`
- Ahora retornará `"vencido"` tanto para membresías expiradas como para clientes que nunca tuvieron membresía

**Estados posibles (NUEVO):**
| Estado | Significado |
|--------|-------------|
| `"activo"` | Membresía vigente y QR activo |
| `"vencido"` | Membresía expirada **O** nunca tuvo membresía (**CAMBIO**) |
| `"qr_deshabilitado"` | QR reportado como perdido/robado |

> ⚠️ **BREAKING CHANGE:** Si tienen filtros o badges que dependen del string `"sin_membresia"`, deben actualizarlos a `"vencido"`.

**En el frontend:**
1. Buscar y reemplazar toda referencia a `"sin_membresia"` por `"vencido"` 
2. Actualizar el badge/etiqueta de estado para que diga "Vencido" con color rojo
3. Los filtros de estado en la tabla de clientes deben usar `"vencido"` en vez de `"sin_membresia"`

---

## 3. 📊 Simplificar Dashboard Principal

**Qué dijo el cliente:**
> "Tendencia, asistencia no creo que sea tan importante. Top clientes, tampoco creo que sea tan importante en inicio."
> "Esas gráficas deberían poder verlo en reportes."

**Lo que QUIERE ver en Dashboard (inicio):**
- ✅ KPI: Clientes activos
- ✅ KPI: Ingresos del mes
- ✅ KPI: Asistencias de hoy
- ✅ Gráfica: Horas pico
- ✅ Lista: Actividad reciente
- ✅ Lista: Membresías por vencer

**Lo que quiere QUITAR del Dashboard (mover a Reportes):**
- ❌ Gráfica de tendencia de asistencias
- ❌ Top clientes por asistencias

**Cambio en API:** Ninguno — los endpoints siguen iguales.

**En el frontend:**
1. Quitar componente "Tendencia de Asistencias" del Dashboard
2. Quitar componente "Top Clientes" del Dashboard
3. Mover ambos a la sección de Reportes (pueden ir en un tab o sección aparte)
4. Mantener el layout limpio y simple en el Dashboard

---

## 4. 🏋️ Control de Presencia: "¿Quién está en el gimnasio?"

**Qué dijo el cliente:**
> "¿Para el momento de salir, cómo sé que ya salió?"
> *Asistente:* "Yo quiero ver quién está adentro entrenando. Si ya se fue y no escaneó, yo voy a pensar que sigue aquí."

**Nuevos endpoints que crearemos en backend:**

### 4.1 Obtener personas en el gimnasio ahora
```
GET /api/asistencia/presentes-ahora
```

**Respuesta:**
```json
{
  "totalPresentes": 12,
  "presentes": [
    {
      "asistenciaId": 452,
      "clienteId": 8,
      "nombreCompleto": "Carlos Mendoza",
      "horaEntrada": "14:30",
      "tiempoTranscurrido": "2h 15min",
      "membresiaActual": "Plan Mensual"
    }
  ]
}
```

### 4.2 Marcar salida manual
```
PATCH /api/asistencia/{id}/marcar-salida
```

**Respuesta:**
```json
{
  "asistenciaId": 452,
  "clienteNombre": "Carlos Mendoza",
  "horaEntrada": "14:30",
  "horaSalida": "16:45",
  "mensaje": "Salida registrada correctamente"
}
```

> **Nota:** Después de 5 horas sin marcar salida, el sistema los marca como "fuera" automáticamente.

**En el frontend:**
1. Agregar widget en Dashboard: **"En el gimnasio ahora: 12"** (con icono de personas)
2. Al hacer clic, mostrar lista expandible con nombre + hora de entrada + tiempo transcurrido
3. Cada fila tiene botón "Marcar salida" (llama al PATCH)
4. Se actualiza automáticamente cada 60 segundos (o pull manual)

---

## 5. 🛒 Módulo Productos / Punto de Caja (Sprint 4)

**Qué dijo el cliente:**
> "No hay control del punto de caja... Si ponemos un escáner, lo ideal sería también un punto de caja."
> *Asistente:* "Ahorita lo manejamos todo por Excel."

**Estado:** 🟡 Planeado para Sprint 4. No hay endpoints aún.

**Entidades planeadas:**
- `Producto` (id, nombre, precio, stock, categoría)
- `Venta` (id, cliente_id, fecha, total, metodo_pago)
- `DetalleVenta` (id, venta_id, producto_id, cantidad, subtotal)

Les avisaremos cuando los endpoints estén listos.

---

## 6. 📱 Escáner QR (USB vs Cámara)

**Qué dijo el cliente:**
> "Podríamos comprar una maquinita para el escaneo del código QR."

**Opciones:**
1. **Escáner USB:** Funciona como teclado. El escáner lee el QR y "escribe" el código. No necesita integración especial — solo un campo de texto que reciba el input.
2. **Cámara web:** Requiere librería JS (`html5-qrcode`). Más complejo pero no necesita hardware extra.

**En el frontend:**
- Si usan escáner USB: Solo asegurarse de que el campo de escaneo tenga autofocus
- Si usan cámara: Integrar `html5-qrcode` en la vista de asistencias

---

## 📊 Resumen de Cambios (Checklist)

### Backend ✅ COMPLETADO
- [x] Agregar campo `dni` a Cliente (entity + DTOs + validación)
- [x] Cambiar `"sin_membresia"` → `"vencido"` en `getEstado()`
- [x] Agregar `hora_salida` a Asistencia
- [x] Crear `GET /api/asistencia/presentes-ahora`
- [x] Crear `PATCH /api/asistencia/{id}/marcar-salida`
- [x] Auto-marcar salida después de 5 horas (en el mismo GET de presentes)

### Frontend (ustedes)
- [ ] Agregar campo DNI en formulario de registro
- [ ] Agregar columna DNI en tabla de clientes
- [ ] Cambiar badge `"sin_membresia"` → `"vencido"` en todos los componentes
- [ ] Quitar Tendencia y Top Clientes del Dashboard → mover a Reportes
- [ ] Agregar widget "En el gimnasio ahora: X"
- [ ] Lista de presentes con botón "Marcar salida"
- [ ] (Sprint 4) Módulo de Productos
- [ ] (Opcional) Integrar cámara para escaneo QR

> **⚠️ NOTA IMPORTANTE:** La URL base es `/api/asistencia` (singular), no `/api/asistencias`.
