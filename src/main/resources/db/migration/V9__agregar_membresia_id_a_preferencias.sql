-- ============================================
-- V9: Agregar columna membresia_id a preferencias_mp
-- ============================================
-- Esta columna permite vincular cada preferencia de pago con la membresía específica seleccionada

ALTER TABLE preferencias_mp 
ADD COLUMN membresia_id BIGINT NULL COMMENT 'ID de la membresía asociada' AFTER plan_dias;

-- Agregar índice para mejorar consultas
CREATE INDEX idx_preferencia_membresia ON preferencias_mp(membresia_id);

-- Agregar foreign key (opcional, si quieres integridad referencial)
ALTER TABLE preferencias_mp
ADD CONSTRAINT fk_preferencia_membresia
FOREIGN KEY (membresia_id) REFERENCES membresias(id)
ON DELETE SET NULL;
