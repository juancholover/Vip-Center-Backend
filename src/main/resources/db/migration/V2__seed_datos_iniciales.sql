-- ============================================
-- V2: Datos iniciales (seed)
-- ============================================
-- Permisos base y rol admin
-- Usuario admin se crea en DataInitializer con variables de entorno

-- ============================================
-- PERMISOS BASE DEL SISTEMA
-- ============================================
-- Permisos organizados por módulo

INSERT INTO permisos (codigo, modulo, accion, descripcion) VALUES
-- Módulo: usuarios (gestión de empleados)
('usuarios.crear', 'usuarios', 'crear', 'Crear nuevos usuarios del sistema'),
('usuarios.ver', 'usuarios', 'ver', 'Ver lista de usuarios'),
('usuarios.editar', 'usuarios', 'editar', 'Editar datos de usuarios'),
('usuarios.desactivar', 'usuarios', 'desactivar', 'Desactivar/activar usuarios'),
('usuarios.asignar_rol', 'usuarios', 'asignar_rol', 'Asignar roles a usuarios'),

-- Módulo: roles (gestión de roles y permisos)
('roles.crear', 'roles', 'crear', 'Crear nuevos roles'),
('roles.ver', 'roles', 'ver', 'Ver lista de roles'),
('roles.editar', 'roles', 'editar', 'Editar roles'),
('roles.asignar_permiso', 'roles', 'asignar_permiso', 'Asignar permisos a roles'),

-- Módulo: clientes (gestión de clientes del gym)
('clientes.crear', 'clientes', 'crear', 'Registrar nuevos clientes'),
('clientes.ver', 'clientes', 'ver', 'Ver lista de clientes'),
('clientes.editar', 'clientes', 'editar', 'Editar datos de clientes'),
('clientes.regenerar_qr', 'clientes', 'regenerar_qr', 'Regenerar código QR de acceso'),

-- Módulo: pagos (gestión de pagos y renovaciones)
('pagos.crear', 'pagos', 'crear', 'Crear registros de pago'),
('pagos.ver_mes', 'pagos', 'ver_mes', 'Ver pagos del mes actual'),
('pagos.ver_todos', 'pagos', 'ver_todos', 'Ver todos los pagos históricos'),
('pagos.confirmar', 'pagos', 'confirmar', 'Confirmar pagos pendientes'),

-- Módulo: membresías (catálogo de planes)
('membresias.crear', 'membresias', 'crear', 'Crear nuevos planes de membresía'),
('membresias.editar', 'membresias', 'editar', 'Editar planes existentes'),
('membresias.ver', 'membresias', 'ver', 'Ver catálogo de membresías'),

-- Módulo: asistencias (control de acceso)
('asistencias.validar', 'asistencias', 'validar', 'Validar entrada por QR'),
('asistencias.ver', 'asistencias', 'ver', 'Ver historial de asistencias'),

-- Módulo: reportes (consultas analíticas)
('reportes.ingresos', 'reportes', 'ingresos', 'Ver reportes de ingresos'),
('reportes.asistencias', 'reportes', 'asistencias', 'Ver reportes de asistencia'),
('reportes.clientes_activos', 'reportes', 'clientes_activos', 'Ver estadísticas de clientes');

-- ============================================
-- ROL: admin (administrador principal)
-- ============================================
-- Tiene acceso total a todo el sistema

INSERT INTO roles (nombre, descripcion, activo, creado_por) 
VALUES ('admin', 'Administrador con acceso total al sistema', TRUE, NULL);

-- ============================================
-- ROL: recepcionista (operaciones básicas)
-- ============================================
-- Puede registrar clientes, pagos y validar asistencias

INSERT INTO roles (nombre, descripcion, activo, creado_por) 
VALUES ('recepcionista', 'Operador de recepción con permisos limitados', TRUE, NULL);

-- ============================================
-- ASIGNAR PERMISOS AL ROL ADMIN
-- ============================================
-- El admin tiene TODOS los permisos

INSERT INTO rol_permisos (rol_id, permiso_id, asignado_por)
SELECT 
    (SELECT id FROM roles WHERE nombre = 'admin') AS rol_id,
    p.id AS permiso_id,
    NULL AS asignado_por
FROM permisos p;

-- ============================================
-- ASIGNAR PERMISOS AL ROL RECEPCIONISTA
-- ============================================
-- Permisos básicos para operaciones del día a día

INSERT INTO rol_permisos (rol_id, permiso_id, asignado_por)
SELECT 
    (SELECT id FROM roles WHERE nombre = 'recepcionista') AS rol_id,
    p.id AS permiso_id,
    NULL AS asignado_por
FROM permisos p
WHERE p.codigo IN (
    -- Puede ver usuarios pero no crearlos
    'usuarios.ver',
    
    -- Gestión completa de clientes
    'clientes.crear',
    'clientes.ver',
    'clientes.editar',
    
    -- Puede crear y ver pagos del mes
    'pagos.crear',
    'pagos.ver_mes',
    'pagos.confirmar',
    
    -- Solo ver catálogo de membresías
    'membresias.ver',
    
    -- Control de acceso
    'asistencias.validar',
    'asistencias.ver',
    
    -- Reportes básicos
    'reportes.asistencias',
    'reportes.clientes_activos'
);

-- ============================================
-- COMENTARIOS
-- ============================================
-- NOTA: El usuario admin se crea en DataInitializer.java
-- usando variables de entorno (ADMIN_EMAIL, ADMIN_PASSWORD)
-- 
-- PERMISOS GRANULARES:
-- - Cada acción tiene un permiso específico
-- - Facilita control de acceso fino
-- - Se validan con @PreAuthorize en controllers
--
-- ROLES EXTENSIBLES:
-- - Puedes crear roles personalizados desde la app
-- - Asigna solo los permisos necesarios
-- - Ejemplos: 'supervisor', 'contador', 'entrenador'
