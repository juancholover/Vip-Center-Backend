-- ================================================================
-- V99: Script de prueba para rellenar tabla CLIENTES
-- ================================================================
-- ADVERTENCIA: Este script es SOLO para desarrollo/testing
-- NO ejecutar en producción sin revisar los datos
-- ================================================================

-- ================================================================
-- Insertar 50 clientes de prueba con datos variados
-- ================================================================

-- Clientes ACTIVOS con membresía vigente (20 clientes)
INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Carlos', 'Rodríguez', '987654321', 'carlos.rodriguez@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 25 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('María', 'González', '987654322', 'maria.gonzalez@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Luis', 'Fernández', '987654323', 'luis.fernandez@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 15 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Ana', 'Martínez', '987654324', 'ana.martinez@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 45 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Jorge', 'López', '987654325', 'jorge.lopez@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 60 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Patricia', 'Sánchez', '987654326', 'patricia.sanchez@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 20 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Roberto', 'Ramírez', '987654327', 'roberto.ramirez@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 10 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Carmen', 'Torres', '987654328', 'carmen.torres@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 35 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Miguel', 'Flores', '987654329', 'miguel.flores@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 50 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Laura', 'Rivera', '987654330', 'laura.rivera@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Diego', 'Morales', '987654331', 'diego.morales@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 40 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Sofía', 'Gutiérrez', '987654332', 'sofia.gutierrez@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 28 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Fernando', 'Herrera', '987654333', 'fernando.herrera@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 90 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Valentina', 'Mendoza', '987654334', 'valentina.mendoza@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 22 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Andrés', 'Vargas', '987654335', 'andres.vargas@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 18 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Isabella', 'Castro', '987654336', 'isabella.castro@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 12 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Ricardo', 'Ortiz', '987654337', 'ricardo.ortiz@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 70 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Camila', 'Romero', '987654338', 'camila.romero@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 33 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Javier', 'Silva', '987654339', 'javier.silva@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 8 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Daniela', 'Reyes', '987654340', 'daniela.reyes@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 55 DAY), 1, NOW());

-- Clientes PRÓXIMOS A VENCER (membresía vence en 1-3 días) (10 clientes)
INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Gabriel', 'Medina', '987654341', 'gabriel.medina@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Lucía', 'Peña', '987654342', 'lucia.pena@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Sebastián', 'Ríos', '987654343', 'sebastian.rios@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Mariana', 'Cruz', '987654344', 'mariana.cruz@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Alejandro', 'Jiménez', '987654345', 'alejandro.jimenez@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Natalia', 'Ruiz', '987654346', 'natalia.ruiz@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Eduardo', 'Campos', '987654347', 'eduardo.campos@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Carolina', 'Navarro', '987654348', 'carolina.navarro@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Pablo', 'Vega', '987654349', 'pablo.vega@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Andrea', 'Paredes', '987654350', 'andrea.paredes@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 1, NOW());

-- Clientes VENCIDOS (membresía expirada) (10 clientes)
INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Martín', 'Salazar', '987654351', 'martin.salazar@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_SUB(CURDATE(), INTERVAL 5 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Gabriela', 'Ibarra', '987654352', 'gabriela.ibarra@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_SUB(CURDATE(), INTERVAL 10 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Nicolás', 'Aguilar', '987654353', 'nicolas.aguilar@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_SUB(CURDATE(), INTERVAL 15 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Valeria', 'Fuentes', '987654354', 'valeria.fuentes@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Santiago', 'Ponce', '987654355', 'santiago.ponce@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_SUB(CURDATE(), INTERVAL 20 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Paula', 'Delgado', '987654356', 'paula.delgado@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_SUB(CURDATE(), INTERVAL 7 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Emilio', 'Cortés', '987654357', 'emilio.cortes@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_SUB(CURDATE(), INTERVAL 12 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Florencia', 'Montes', '987654358', 'florencia.montes@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_SUB(CURDATE(), INTERVAL 8 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Mateo', 'Acosta', '987654359', 'mateo.acosta@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_SUB(CURDATE(), INTERVAL 25 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Jazmín', 'León', '987654360', 'jazmin.leon@email.com', REPLACE(UUID(), '-', ''), TRUE, DATE_SUB(CURDATE(), INTERVAL 4 DAY), 1, NOW());

-- Clientes SIN MEMBRESÍA (nuevos, aún no han comprado plan) (5 clientes)
INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Tomás', 'Sandoval', '987654361', 'tomas.sandoval@email.com', REPLACE(UUID(), '-', ''), TRUE, NULL, 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Renata', 'Mora', '987654362', 'renata.mora@email.com', REPLACE(UUID(), '-', ''), TRUE, NULL, 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Bruno', 'Lara', '987654363', 'bruno.lara@email.com', REPLACE(UUID(), '-', ''), TRUE, NULL, 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Victoria', 'Escobar', '987654364', 'victoria.escobar@email.com', REPLACE(UUID(), '-', ''), TRUE, NULL, 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Maximiliano', 'Rojas', '987654365', 'maximiliano.rojas@email.com', REPLACE(UUID(), '-', ''), TRUE, NULL, 1, NOW());

-- Clientes con QR DESHABILITADO (reportados por pérdida/robo) (5 clientes)
INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Lucas', 'Pacheco', '987654366', 'lucas.pacheco@email.com', REPLACE(UUID(), '-', ''), FALSE, DATE_ADD(CURDATE(), INTERVAL 20 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Jimena', 'Carrillo', '987654367', 'jimena.carrillo@email.com', REPLACE(UUID(), '-', ''), FALSE, DATE_ADD(CURDATE(), INTERVAL 15 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Francisco', 'Quintana', '987654368', 'francisco.quintana@email.com', REPLACE(UUID(), '-', ''), FALSE, DATE_ADD(CURDATE(), INTERVAL 30 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Catalina', 'Miranda', '987654369', 'catalina.miranda@email.com', REPLACE(UUID(), '-', ''), FALSE, DATE_ADD(CURDATE(), INTERVAL 10 DAY), 1, NOW());

INSERT INTO clientes (nombre, apellido, telefono, email, qr_acceso, qr_activo, fecha_vencimiento, registrado_por, fecha_registro) VALUES
('Ignacio', 'Cabrera', '987654370', 'ignacio.cabrera@email.com', REPLACE(UUID(), '-', ''), FALSE, DATE_ADD(CURDATE(), INTERVAL 25 DAY), 1, NOW());

-- ================================================================
-- VERIFICACIÓN: Contar clientes por estado
-- ================================================================
-- Ejecutar estos queries para verificar la inserción:
/*
SELECT 
    CASE 
        WHEN fecha_vencimiento IS NULL THEN 'sin_membresia'
        WHEN qr_activo = FALSE THEN 'qr_deshabilitado'
        WHEN fecha_vencimiento >= CURDATE() THEN 'activo'
        WHEN fecha_vencimiento < CURDATE() THEN 'vencido'
    END AS estado,
    COUNT(*) as cantidad
FROM clientes
GROUP BY estado;

-- Resultado esperado:
-- activo: 20
-- proximo_vencer: 10 (incluidos en activos, pero vencen en 1-3 días)
-- vencido: 10
-- sin_membresia: 5
-- qr_deshabilitado: 5
-- TOTAL: 50 clientes
*/

-- ================================================================
-- LIMPIEZA (solo si necesitas borrar los datos de prueba)
-- ================================================================
/*
DELETE FROM clientes WHERE telefono LIKE '98765%';
*/
