# VIP Center Fit - Backend 🏋️‍♂️

Backend del sistema integral de gestión para gimnasios **VIP Center Fit**. Esta API REST robusta y escalable está desarrollada utilizando **Java 17** y **Spring Boot 3**. Proporciona toda la lógica de negocio y persistencia de datos necesaria para la administración del gimnasio, desde la gestión de clientes hasta el control de asistencia y reportes financieros.

## 🚀 Características Principales

*   **Gestión de Clientes:** Registro, actualización e historial de clientes y preferencias.
*   **Membresías y Suscripciones:** Administración de planes de membresía, aplicación de descuentos y control de renovaciones.
*   **Control de Accesos:** Validación de ingresos mediante códigos QR y registro de historial de asistencia.
*   **Gestión Financiera:** Registro de pagos, métodos de pago e historial detallado de transacciones (incluyendo pagos fallidos).
*   **Reportes y Estadísticas:** Análisis de ingresos (por método o plan), retención mensual, tendencias de asistencia y seguimiento de clientes inactivos.
*   **Notificaciones:** Alertas automatizadas por vencimiento de membresías y seguimiento especializado.
*   **Seguridad:** Autenticación robusta con listado negro de tokens (Blacklist).

## 🛠️ Stack Tecnológico

*   **Lenguaje:** Java 17
*   **Framework Core:** Spring Boot 3
*   **Seguridad:** Spring Security
*   **Persistencia:** Spring Data JPA / Hibernate
*   **Migraciones de Base de Datos:** Flyway
*   **Gestor de Dependencias:** Maven
*   **Base de Datos Relacional:** (Configurable en `application.properties`)

## 📋 Requisitos Previos

*   [Java Development Kit (JDK) 17](https://www.oracle.com/java/technologies/downloads/#java17)
*   [Apache Maven](https://maven.apache.org/) (recomendado, aunque se incluye Maven Wrapper `mvnw`)
*   Motor de base de datos (PostgreSQL/MySQL)

## ⚙️ Instalación y Configuración

1.  **Clonar el repositorio:**
    ```bash
    git clone https://github.com/juancholover/Vip-Center-Backend.git
    cd Vip-Center-Backend
    ```

2.  **Configurar Variables de Entorno / Base de Datos:**
    Revisa y ajusta las credenciales en el archivo `src/main/resources/application.properties` (o el perfil activo de Spring) para apuntar a tu base de datos.

3.  **Ejecutar el proyecto:**
    Puedes encender la aplicación utilizando el Wrapper de Maven incluido:
    
    Para Windows:
    ```cmd
    .\mvnw.cmd clean install
    .\mvnw.cmd spring-boot:run
    ```
    
    *Nota: Las migraciones de Flyway (ubicadas en `src/main/resources/db/migration/`) se ejecutarán automáticamente al arrancar, creando todo el modelo relacional y realizando el seed de datos.*

## 📖 Estructura del Proyecto

*   `controller/` - Exposición de endpoints REST.
*   `service/` - Lógica de negocio (Gestión de inactividad, cron jobs, etc.).
*   `repository/` - Interfaces JPA para el acceso transaccional a los datos.
*   `entity/` - Modelado ORM y entidades del dominio de negocio.
*   `dto/` - Objetos Data Transfer formados para las vistas Frontend y App móvil.
*   `config/` - Configuraciones generales (Security, Swagger, Web, etc.).
*   `db/migration/` - Scripts de Flyway.

## 👥 Repositorio

*   **Repositorio Web:** [Vip-Center-Backend](https://github.com/juancholover/Vip-Center-Backend)
*   Mantenido por juancholover y el equipo técnico de VIP Center Fit.
