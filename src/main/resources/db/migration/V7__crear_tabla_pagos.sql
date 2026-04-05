-- ============================================
-- V7: Crear tabla PAGOS
-- ============================================
-- Registro de todos los pagos del gimnasio

CREATE TABLE pagos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Relación con cliente
    cliente_id BIGINT NOT NULL COMMENT 'Cliente que realizó el pago',
    
    -- Datos del plan
    plan_nombre VARCHAR(80) NOT NULL COMMENT 'Nombre del plan (MENSUAL, TRIMESTRAL, etc.)',
    plan_dias INT NOT NULL COMMENT 'Días de duración del plan',
    monto_final DECIMAL(12,2) NOT NULL COMMENT 'Monto pagado',
    
    -- Estado del pago
    estado VARCHAR(20) NOT NULL DEFAULT 'pendiente' COMMENT 'pendiente, aprobado, rechazado, reembolsado',
    
    -- Datos de MercadoPago
    mp_preference_id VARCHAR(64) NULL COMMENT 'ID de preferencia de MercadoPago',
    mp_payment_id VARCHAR(64) NULL COMMENT 'ID de pago de MercadoPago',
    mp_payload LONGTEXT NULL COMMENT 'JSON completo del webhook de MercadoPago',
    
    -- Auditoría
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha de creación del registro',
    
    -- Índices
    INDEX idx_pago_cliente (cliente_id),
    INDEX idx_pago_mp (mp_preference_id, mp_payment_id),
    INDEX idx_pago_estado (estado),
    INDEX idx_pago_fecha (fecha_registro),
    
    -- Foreign key
    CONSTRAINT fk_pago_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES clientes(id)
        ON DELETE CASCADE
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Registro de pagos del gimnasio';

-- ============================================
-- COMENTARIOS IMPORTANTES
-- ============================================
-- ESTADOS POSIBLES:
-- - pendiente: pago creado, esperando confirmación
-- - aprobado: pago confirmado, membresía activada
-- - rechazado: pago rechazado por MercadoPago
-- - reembolsado: pago devuelto al cliente
--
-- FLUJO NORMAL:
-- 1. POST /api/pagos/crear -> crea preferencia en MercadoPago
-- 2. Cliente paga -> webhook recibe notificación
-- 3. Estado cambia a 'aprobado'
-- 4. Se actualiza fecha_vencimiento del cliente
--
-- FLUJO YAPE:
-- 1. POST /api/pagos/crear-con-yape -> pago directo con token
-- 2. Si aprobado inmediatamente -> estado 'aprobado'
-- 3. Si pendiente -> estado 'pendiente', webhook confirma después
--
-- mp_payload:
-- - Guarda el JSON completo del webhook
-- - Útil para debugging y auditoría
-- - LONGTEXT soporta hasta 4GB
