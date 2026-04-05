package com.gimnasio.fit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_acceso", indexes = {
    @Index(name = "idx_usuario_id", columnList = "usuario_id"),
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_tipo_evento", columnList = "tipo_evento"),
    @Index(name = "idx_fecha_hora", columnList = "fecha_hora")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialAcceso {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;
    
    @Column(nullable = false, length = 100)
    private String username;
    
    @Column(name = "tipo_evento", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private TipoEvento tipoEvento;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    @Column(columnDefinition = "TEXT")
    private String detalles;
    
    @Column(nullable = false)
    private Boolean exitoso = true;
    
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
    
    @PrePersist
    protected void onCreate() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
        if (exitoso == null) {
            exitoso = true;
        }
    }
    
    // Enum para tipos de eventos
    public enum TipoEvento {
        LOGIN("Inicio de sesión exitoso"),
        LOGOUT("Cierre de sesión"),
        LOGIN_FAILED("Intento de inicio de sesión fallido"),
        PASSWORD_CHANGE("Cambio de contraseña"),
        PASSWORD_RESET("Restablecimiento de contraseña"),
        PROFILE_UPDATE("Actualización de perfil"),
        TOKEN_REFRESH("Renovación de token"),
        UNAUTHORIZED_ACCESS("Acceso no autorizado"),
        ACCOUNT_LOCKED("Cuenta bloqueada"),
        ACCOUNT_UNLOCKED("Cuenta desbloqueada");
        
        private final String descripcion;
        
        TipoEvento(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
}
