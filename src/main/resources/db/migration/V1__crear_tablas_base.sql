-- ============================================
-- V1: Crear tablas base del sistema
-- ============================================
-- Tablas para gestión de usuarios, roles y permisos (sistema interno)

-- ============================================
-- TABLA: usuarios
-- ============================================
-- Usuarios del sistema (SOLO empleados y dueño)
-- Los CLIENTES no están aquí

CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Credenciales
    email VARCHAR(100) NOT NULL UNIQUE COMMENT 'Email único para login',
    password_hash VARCHAR(255) NOT NULL COMMENT 'Contraseña cifrada con BCrypt',
    
    -- Datos personales
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NULL,
    
    -- Estado y control
    activo BOOLEAN DEFAULT TRUE COMMENT 'false = cuenta desactivada',
    
    -- Auditoría
    creado_por BIGINT NULL COMMENT 'Usuario que creó esta cuenta',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso TIMESTAMP NULL COMMENT 'Última vez que hizo login',
    
    -- Índices para búsquedas rápidas
    INDEX idx_email (email),
    INDEX idx_activo (activo),
    
    -- Auto-referencia para auditoría
    CONSTRAINT fk_usuario_creado_por 
        FOREIGN KEY (creado_por) 
        REFERENCES usuarios(id) 
        ON DELETE SET NULL
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Usuarios del sistema (empleados y admin)';

-- ============================================
-- TABLA: roles
-- ============================================
-- Roles del sistema (admin, recepcionista, etc.)

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    nombre VARCHAR(50) NOT NULL UNIQUE COMMENT 'Nombre único del rol (ej: admin)',
    descripcion TEXT NULL,
    activo BOOLEAN DEFAULT TRUE,
    
    -- Auditoría
    creado_por BIGINT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_nombre (nombre),
    INDEX idx_activo (activo),
    
    CONSTRAINT fk_rol_creado_por 
        FOREIGN KEY (creado_por) 
        REFERENCES usuarios(id) 
        ON DELETE SET NULL
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Roles del sistema';

-- ============================================
-- TABLA: permisos
-- ============================================
-- Permisos granulares del sistema

CREATE TABLE permisos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    codigo VARCHAR(50) NOT NULL UNIQUE COMMENT 'Llave lógica única (ej: clientes.crear)',
    modulo VARCHAR(50) NOT NULL COMMENT 'Módulo al que pertenece (ej: clientes)',
    accion VARCHAR(50) NOT NULL COMMENT 'Acción permitida (ej: crear)',
    descripcion TEXT NULL,
    
    INDEX idx_codigo (codigo),
    INDEX idx_modulo_accion (modulo, accion),
    
    UNIQUE KEY uk_permiso_codigo (codigo)
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Permisos granulares del sistema';

-- ============================================
-- TABLA: usuario_roles
-- ============================================
-- Relación muchos a muchos: Usuario <-> Rol

CREATE TABLE usuario_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    usuario_id BIGINT NOT NULL,
    rol_id BIGINT NOT NULL,
    
    -- Auditoría
    asignado_por BIGINT NULL COMMENT 'Usuario que hizo la asignación',
    fecha_asignacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Índice único: un usuario no puede tener el mismo rol 2 veces
    UNIQUE KEY uk_usuario_rol (usuario_id, rol_id),
    
    CONSTRAINT fk_usuario_rol_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) 
        ON DELETE CASCADE,
        
    CONSTRAINT fk_usuario_rol_rol 
        FOREIGN KEY (rol_id) 
        REFERENCES roles(id) 
        ON DELETE CASCADE,
        
    CONSTRAINT fk_usuario_rol_asignado_por 
        FOREIGN KEY (asignado_por) 
        REFERENCES usuarios(id) 
        ON DELETE SET NULL
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Asignación de roles a usuarios';

-- ============================================
-- TABLA: rol_permisos
-- ============================================
-- Relación muchos a muchos: Rol <-> Permiso

CREATE TABLE rol_permisos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    rol_id BIGINT NOT NULL,
    permiso_id BIGINT NOT NULL,
    
    -- Auditoría
    asignado_por BIGINT NULL,
    fecha_asignacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Índice único: un rol no puede tener el mismo permiso 2 veces
    UNIQUE KEY uk_rol_permiso (rol_id, permiso_id),
    
    CONSTRAINT fk_rol_permiso_rol 
        FOREIGN KEY (rol_id) 
        REFERENCES roles(id) 
        ON DELETE CASCADE,
        
    CONSTRAINT fk_rol_permiso_permiso 
        FOREIGN KEY (permiso_id) 
        REFERENCES permisos(id) 
        ON DELETE CASCADE,
        
    CONSTRAINT fk_rol_permiso_asignado_por 
        FOREIGN KEY (asignado_por) 
        REFERENCES usuarios(id) 
        ON DELETE SET NULL
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Asignación de permisos a roles';

-- ============================================
-- COMENTARIOS IMPORTANTES
-- ============================================
-- USUARIOS:
-- - Representan SOLO empleados (admin, recepcionista, etc.)
-- - Los CLIENTES del gimnasio están en tabla separada
-- - creado_por: permite auditar quién creó cada cuenta
-- - ultimo_acceso: se actualiza en cada login exitoso
--
-- ROLES Y PERMISOS:
-- - Arquitectura flexible (RBAC: Role-Based Access Control)
-- - Un usuario puede tener múltiples roles
-- - Un rol puede tener múltiples permisos
-- - Ejemplos de permisos:
--   * clientes.crear, clientes.ver, clientes.editar
--   * pagos.crear, pagos.confirmar
--   * reportes.ingresos, reportes.asistencias
--
-- CASCADE vs SET NULL:
-- - CASCADE: si borro usuario/rol, borro sus relaciones
-- - SET NULL: si borro usuario, sus auditorías quedan con NULL
