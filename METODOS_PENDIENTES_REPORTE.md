# Documentación de Cambios Diferidos (Work in Progress)
**Fecha:** 14 de mayo de 2026
**Motivo:** Despliegue inicial a producción (Render)

Durante el proceso de compilación en Render, la construcción fallaba ya que existían llamadas a ciertos métodos en los Servicios (`ReportesService`, `DashboardService`) que aún no estaban completamente implementados o definidos, lo que causaba el error de compilación `cannot find symbol`.

Para permitir que el proyecto compile y se pueda publicar correctamente, hemos "comentado" temporalmente estos endpoints en sus respectivos controladores, haciendo que devuelvan listas o páginas vacías.

## Archivos modificados y endpoints afectados

### 1. `ReportesController.java`
Se comentaron temporalmente las llamadas a `reportesService` y se reemplazaron por retornos simulados (mocks vacíos) en los siguientes endpoints:

* **Suscripciones**:
  * `obtenerReporteSuscripciones` -> Devuelve `Page.empty()`
  * `exportarSuscripcionesExcel` -> Devuelve `new byte[0]`
* **Ingresos**:
  * `obtenerIngresosPorMetodo` -> Devuelve `Collections.emptyList()`
  * `obtenerIngresosPorPlan` -> Devuelve `Collections.emptyList()`
* **Historial y Retención**:
  * `obtenerHistorialPagosDetallado` -> Devuelve `Collections.emptyList()`
  * `obtenerRetencionMensual` -> Devuelve `Collections.emptyList()`
  * `exportarHistorialPagosExcel` -> Devuelve `new byte[0]`

### 2. `DashboardController.java`
Se comentaron temporalmente las llamadas a `dashboardService` en:

* **Tendencia de asistencias**:
  * `obtenerTendenciaAsistencias` -> Devuelve `Collections.emptyList()`
* **Top de Clientes**:
  * `obtenerTopClientes` -> Devuelve `Collections.emptyList()`

---

## 🛠️ ¿Cómo retomar el trabajo?

Cuando estés listo para desarrollar la lógica de estos reportes y métricas del dashboard:

1. Ve a los controladores (`ReportesController.java` y `DashboardController.java`).
2. Descomenta las líneas que llaman al servicio (ejemplo: `List<...> resultado = ...Service.obtener...(...)`).
3. Elimina las líneas simuladas (las que dicen `Collections.emptyList()`, `Page.empty()` o `new byte[0]`).
4. **Importante:** Asegúrate de ir a las clases `ReportesService.java` y `DashboardService.java` y **crear la implementación real** de esos métodos para que tu IDE y el compilador de Maven (y Render) ya no marquen el error de que el método "no existe".

---
*(Otros cambios realizados durante esta etapa incluyeron la creación del `Dockerfile` limitando el consumo de RAM a 300MB y la extracción de credenciales duras del `application.properties` para usar variables de entorno `*.env`)*