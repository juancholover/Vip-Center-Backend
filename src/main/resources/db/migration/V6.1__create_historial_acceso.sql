-- Tabla para auditoría de accesos de usuarios
CREATE TABLE historial_acceso (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    username VARCHAR(100) NOT NULL,
    tipo_evento VARCHAR(50) NOT NULL, -- LOGIN, LOGOUT, LOGIN_FAILED, PASSWORD_CHANGE, etc.
    ip_address VARCHAR(45), -- IPv4 o IPv6
    user_agent TEXT, -- Navegador/cliente
    detalles TEXT, -- JSON con información adicional
    exitoso BOOLEAN DEFAULT TRUE,
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_historial_usuario FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) ON DELETE CASCADE,
    
    INDEX idx_usuario_id (usuario_id),
    INDEX idx_username (username),
    INDEX idx_tipo_evento (tipo_evento),
    INDEX idx_fecha_hora (fecha_hora),
    INDEX idx_exitoso (exitoso)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comentarios para documentación
ALTER TABLE historial_acceso 
    COMMENT = 'Registro de auditoría de accesos y acciones de usuarios';
