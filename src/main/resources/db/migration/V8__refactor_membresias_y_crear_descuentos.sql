-- ============================================
-- V8: Refactorizar tabla membresías y crear tabla descuentos
-- ============================================

-- 1. Renombrar columna 'activa' a 'estado' en membresías
ALTER TABLE membresias 
RENAME COLUMN activa TO estado;

-- 2. Eliminar columnas innecesarias de membresías
ALTER TABLE membresias 
DROP COLUMN IF EXISTS codigo,
DROP COLUMN IF EXISTS beneficios,
DROP COLUMN IF EXISTS precio_descuento;

-- 3. Crear tabla descuentos
CREATE TABLE IF NOT EXISTS descuentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL COMMENT 'Nombre del descuento (ej: Estudiante, Adulto Mayor)',
    porcentaje DECIMAL(5,2) NOT NULL COMMENT 'Porcentaje de descuento (ej: 20.00 = 20%)',
    orden INT NOT NULL DEFAULT 0 COMMENT 'Orden de visualización (menor a mayor porcentaje)',
    estado BIT(1) NOT NULL DEFAULT 1 COMMENT '1=activo, 0=inactivo',
    fecha_creacion DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    fecha_modificacion DATETIME(6) DEFAULT NULL,
    creado_por BIGINT DEFAULT NULL,
    modificado_por BIGINT DEFAULT NULL,
    
    CONSTRAINT uk_descuento_nombre UNIQUE (nombre),
    CONSTRAINT fk_descuento_creado_por FOREIGN KEY (creado_por) REFERENCES empleados(id),
    CONSTRAINT fk_descuento_modificado_por FOREIGN KEY (modificado_por) REFERENCES empleados(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Insertar descuentos iniciales (ordenados por porcentaje)
INSERT INTO descuentos (nombre, porcentaje, orden, estado) VALUES
('Ninguno', 0.00, 0, 1),
('Estudiante', 10.00, 1, 1),
('Adulto Mayor', 15.00, 2, 1),
('Promoción Especial', 20.00, 3, 1);

-- 5. Crear índices para optimizar consultas
CREATE INDEX idx_descuentos_estado ON descuentos(estado);
CREATE INDEX idx_descuentos_orden ON descuentos(orden);
CREATE INDEX idx_membresias_estado ON membresias(estado);
CREATE INDEX idx_membresias_orden ON membresias(orden);

-- 6. Comentario de auditoría
INSERT INTO schema_version_comments (version, comentario) 
VALUES (8, 'Refactorización de membresías: eliminadas columnas codigo, beneficios, precio_descuento. Renombrada activa->estado. Creada tabla descuentos con orden automático.')
ON DUPLICATE KEY UPDATE comentario = VALUES(comentario);
