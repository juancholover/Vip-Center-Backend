-- ============================================
-- V5: Crear tabla para tokens invalidados
-- ============================================
-- Sistema de blacklist para JWT tokens

CREATE TABLE tokens_invalidos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Hash del token JWT (para no almacenar token completo)
    token_hash VARCHAR(64) NOT NULL UNIQUE COMMENT 'SHA-256 hash del token JWT',
    
    -- Fecha de expiración del token original
    fecha_expiracion TIMESTAMP NOT NULL COMMENT 'Cuándo expira naturalmente el token',
    
    -- Auditoría
    fecha_invalidacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Cuándo se invalidó',
    motivo VARCHAR(100) NULL COMMENT 'Razón de invalidación (logout, cambio password, etc)',
    usuario_id BIGINT NULL COMMENT 'Usuario al que pertenecía el token',
    
    -- Índices para búsquedas rápidas
    INDEX idx_token_hash (token_hash),
    INDEX idx_fecha_expiracion (fecha_expiracion),
    INDEX idx_usuario_id (usuario_id),
    
    -- Constraint único en el hash
    UNIQUE KEY uk_token_hash (token_hash),
    
    -- Relación con usuarios
    CONSTRAINT fk_token_invalido_usuario 
        FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) 
        ON DELETE CASCADE
        
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Blacklist de tokens JWT invalidados (logout, cambio password)';

-- ============================================
-- COMENTARIOS SOBRE FUNCIONAMIENTO
-- ============================================
-- 
-- PROPÓSITO:
-- - Invalidar tokens JWT antes de su expiración natural
-- - Casos de uso:
--   1. Usuario hace logout explícito
--   2. Admin invalida sesiones de un usuario
--   3. Usuario cambia su contraseña (invalida tokens anteriores)
--   4. Usuario reporta sesión comprometida
--
-- PROCESO DE VALIDACIÓN:
-- 1. JwtAuthenticationFilter recibe token en cada request
-- 2. Verifica firma y expiración (normal)
-- 3. Calcula SHA-256 del token
-- 4. Busca hash en tokens_invalidos
-- 5. Si existe → rechazar request (401 Unauthorized)
-- 6. Si no existe → continuar normalmente
--
-- LIMPIEZA AUTOMÁTICA:
-- - Scheduled task ejecuta cada hora:
--   DELETE FROM tokens_invalidos WHERE fecha_expiracion < NOW()
-- - Evita acumulación infinita de registros
-- - Los tokens expirados naturalmente ya no se pueden usar
--
-- OPTIMIZACIÓN:
-- - Índice único en token_hash → búsqueda O(1)
-- - Índice en fecha_expiracion → limpieza eficiente
-- - Solo almacena hash (no token completo) por seguridad
--
-- ALMACENAMIENTO DE HASH:
-- - SHA-256 produce 64 caracteres hexadecimales
-- - Irreversible: no se puede recuperar token original
-- - Colisión prácticamente imposible
