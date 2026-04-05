-- ============================================
-- V4: Agregar campos de seguridad a usuarios
-- ============================================
-- Campos para control de acceso seguro

-- ============================================
-- 1. debe_cambiar_password
-- ============================================
-- Fuerza al usuario a cambiar su contraseña en el primer login
-- Útil para:
-- - Cuentas recién creadas por admin
-- - Usuario administrador inicial
-- - Reset de contraseña por admin

ALTER TABLE usuarios 
ADD COLUMN debe_cambiar_password BOOLEAN DEFAULT FALSE 
COMMENT 'TRUE = debe cambiar contraseña en próximo login';

-- ============================================
-- 2. intentos_fallidos
-- ============================================
-- Contador de intentos de login fallidos consecutivos
-- Se resetea a 0 tras login exitoso

ALTER TABLE usuarios 
ADD COLUMN intentos_fallidos INT DEFAULT 0 
COMMENT 'Contador de intentos de login fallidos consecutivos';

-- ============================================
-- 3. fecha_bloqueo
-- ============================================
-- Timestamp del bloqueo temporal por múltiples intentos fallidos
-- NULL = no bloqueado
-- Si tiene valor, usuario no puede loguearse hasta que admin lo desbloquee

ALTER TABLE usuarios 
ADD COLUMN fecha_bloqueo TIMESTAMP NULL 
COMMENT 'Fecha de bloqueo por intentos fallidos. NULL = no bloqueado';

-- ============================================
-- ÍNDICES PARA CONSULTAS DE SEGURIDAD
-- ============================================

-- Búsqueda rápida de usuarios bloqueados
CREATE INDEX idx_usuarios_bloqueados ON usuarios(fecha_bloqueo);

-- Búsqueda de usuarios que deben cambiar password
CREATE INDEX idx_usuarios_debe_cambiar_password ON usuarios(debe_cambiar_password);

-- ============================================
-- COMENTARIOS SOBRE USO
-- ============================================
-- 
-- DEBE_CAMBIAR_PASSWORD:
-- 1. Al crear usuario admin inicial → debe_cambiar_password = TRUE
-- 2. Al crear usuario nuevo por admin → puede marcar TRUE
-- 3. En login: si TRUE → devolver error y exigir /change-password
-- 4. Tras cambio exitoso → debe_cambiar_password = FALSE
--
-- INTENTOS_FALLIDOS:
-- 1. Login fallido → intentos_fallidos++
-- 2. Si intentos_fallidos >= 5 → fecha_bloqueo = NOW(), intentos_fallidos = 0
-- 3. Login exitoso → intentos_fallidos = 0
-- 4. Admin puede desbloquear → fecha_bloqueo = NULL
--
-- FECHA_BLOQUEO:
-- 1. Si NOT NULL → rechazar login con error 'Cuenta bloqueada'
-- 2. Admin endpoint: PUT /usuarios/{id}/desbloquear → fecha_bloqueo = NULL
-- 3. Opcional: auto-desbloqueo tras X horas (implementar en service)
