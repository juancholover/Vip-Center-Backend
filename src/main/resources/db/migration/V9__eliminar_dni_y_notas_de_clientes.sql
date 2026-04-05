-- ================================================================
-- Migration V9: Eliminar columnas dni y notas de tabla clientes
-- ================================================================
-- Fecha: 2025-10-20
-- Descripción: Simplificación del modelo de cliente eliminando
--              campos no necesarios (dni, notas)
-- ================================================================

-- Eliminar columna dni
ALTER TABLE clientes DROP COLUMN IF EXISTS dni;

-- Eliminar columna notas
ALTER TABLE clientes DROP COLUMN IF EXISTS notas;
