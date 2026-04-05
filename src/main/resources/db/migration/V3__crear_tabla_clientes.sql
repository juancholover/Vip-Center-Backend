-- ============================================
-- V3: Crear tabla CLIENTES
-- ============================================
-- Clientes del gimnasio (NO son usuarios del sistema)
-- QR se genera en el primer pago confirmado

CREATE TABLE clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Datos personales
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    email VARCHAR(100) NULL,
    dni VARCHAR(20) NULL,
    
    -- QR de acceso (generado tras primer pago)
    qr_acceso VARCHAR(64) NULL UNIQUE COMMENT 'Código QR único para acceso (UUID/SHA256)',
    qr_activo BOOLEAN DEFAULT TRUE COMMENT 'false si fue reportado perdido/robado',
    
    -- Estado de membresía
    fecha_vencimiento DATE NULL COMMENT 'Se actualiza automáticamente al confirmar pago',
    
    -- Auditoría
    registrado_por BIGINT NULL COMMENT 'Usuario que registró al cliente',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notas TEXT NULL COMMENT 'Notas internas sobre el cliente',
    
    -- Índices para búsquedas rápidas
    INDEX idx_telefono (telefono),
    INDEX idx_qr_acceso (qr_acceso),
    INDEX idx_fecha_vencimiento (fecha_vencimiento),
    INDEX idx_qr_estado (qr_acceso, qr_activo),
    
    -- CONSTRAINT CRÍTICO: evita duplicados por nombre+apellido+telefono
    UNIQUE KEY uk_cliente_unico (nombre, apellido, telefono),
    
    -- Relación con usuarios (quien lo registró)
    CONSTRAINT fk_cliente_registrado_por 
        FOREIGN KEY (registrado_por) 
        REFERENCES usuarios(id) 
        ON DELETE SET NULL
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Clientes del gimnasio (NO son usuarios del sistema)';

-- ============================================
-- COMENTARIOS IMPORTANTES
-- ============================================
-- ESTADO DINÁMICO (calculado en tiempo real):
-- - Activo: fecha_vencimiento >= HOY AND qr_activo = true
-- - Vencido: fecha_vencimiento < HOY
-- - Sin membresía: fecha_vencimiento IS NULL
-- - QR deshabilitado: qr_activo = false
--
-- QR DE ACCESO:
-- - Se genera SOLO en el primer pago confirmado
-- - varchar(64) soporta UUID v4 (36 chars) o SHA256 base64 (44 chars)
-- - qr_activo=false si fue reportado perdido/robado
--
-- ACTUALIZACIÓN fecha_vencimiento:
-- - Se actualiza automáticamente al confirmar pago
-- - NUNCA actualizar manualmente sin crear registro en pagos
--
-- UNICIDAD:
-- - (nombre + apellido + telefono) debe ser único
-- - Evita duplicados pero permite homónimos con teléfono diferente
