-- ============================================
-- V6: Crear tabla PREFERENCIAS_MP
-- ============================================
-- Preferencias de pago creadas en MercadoPago
-- Permite recuperar initPoint, generar QR y enviar por WhatsApp

CREATE TABLE preferencias_mp (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Datos de MercadoPago
    preference_id VARCHAR(100) NOT NULL UNIQUE COMMENT 'ID de preferencia retornado por MercadoPago',
    init_point VARCHAR(500) NOT NULL COMMENT 'URL de pago (initPoint)',
    
    -- Relación con cliente
    cliente_id BIGINT NOT NULL COMMENT 'Cliente asociado a esta preferencia',
    
    -- Datos del plan
    monto DECIMAL(12,2) NOT NULL COMMENT 'Monto de la preferencia',
    plan_nombre VARCHAR(100) NULL COMMENT 'Nombre del plan (MENSUAL, TRIMESTRAL, etc.)',
    plan_dias INT NULL COMMENT 'Días de duración del plan',
    email_cliente VARCHAR(100) NULL COMMENT 'Email del cliente',
    
    -- Estado y fechas
    estado VARCHAR(20) NOT NULL DEFAULT 'CREADA' COMMENT 'CREADA, PAGADA, EXPIRADA, CANCELADA',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación',
    fecha_expiracion TIMESTAMP NULL COMMENT 'Fecha de expiración (opcional)',
    
    -- Notas
    notas TEXT NULL COMMENT 'Notas adicionales',
    
    -- Índices
    INDEX idx_pref_mp_preference_id (preference_id),
    INDEX idx_pref_mp_cliente (cliente_id),
    INDEX idx_pref_mp_estado (estado),
    
    -- Foreign key
    CONSTRAINT fk_preferencia_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES clientes(id)
        ON DELETE CASCADE
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Preferencias de pago creadas en MercadoPago';

-- ============================================
-- COMENTARIOS IMPORTANTES
-- ============================================
-- ESTADOS POSIBLES:
-- - CREADA: preferencia recién creada, pendiente de pago
-- - PAGADA: pago confirmado (vinculada a registro en tabla pagos)
-- - EXPIRADA: preferencia expirada sin pago
-- - CANCELADA: preferencia cancelada manualmente
--
-- FLUJO:
-- 1. POST /api/pagos/crear -> crea preferencia y guarda en esta tabla
-- 2. Cliente paga -> webhook actualiza estado a PAGADA
-- 3. Si expira sin pago -> estado EXPIRADA
--
-- initPoint:
-- - URL de pago de MercadoPago
-- - Se usa para generar QR y enviar por WhatsApp
-- - Puede expirar según configuración de MercadoPago
