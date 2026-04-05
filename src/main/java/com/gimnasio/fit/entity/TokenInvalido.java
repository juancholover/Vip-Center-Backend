package com.gimnasio.fit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Entidad para blacklist de tokens JWT invalidados.
 * Permite implementar logout y revocación de tokens antes de su expiración natural.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "tokens_invalidos")
public class TokenInvalido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * Hash SHA-256 del token JWT.
     * No almacenamos el token completo por seguridad.
     */
    @Column(name="token_hash", nullable=false, unique=true, length=64)
    private String tokenHash;

    /**
     * Fecha de expiración natural del token.
     * Permite limpieza automática de registros obsoletos.
     */
    @Column(name="fecha_expiracion", nullable=false)
    private Instant fechaExpiracion;

    /**
     * Fecha en que se invalidó el token.
     */
    @Column(name="fecha_invalidacion", insertable=false, updatable=false)
    private Instant fechaInvalidacion;

    /**
     * Motivo de la invalidación.
     * Ejemplos: "logout", "cambio_password", "admin_revoked", "security_breach"
     */
    @Column(length=100)
    private String motivo;

    /**
     * Usuario al que pertenecía el token.
     * Permite invalidar todos los tokens de un usuario si es necesario.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="usuario_id")
    private Usuario usuario;
}
