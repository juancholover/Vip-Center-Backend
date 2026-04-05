package com.gimnasio.fit.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Entidad que representa el registro de asistencia de un cliente al gimnasio.
 * Almacena información sobre cuándo y cómo se registró la entrada.
 */
@Entity
@Table(name = "asistencias", indexes = {
        @Index(name = "idx_asistencias_cliente", columnList = "cliente_id"),
        @Index(name = "idx_asistencias_fecha", columnList = "fecha_hora"),
        @Index(name = "idx_asistencias_tipo", columnList = "tipo_registro")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_registro", nullable = false, length = 20)
    @Builder.Default
    private TipoRegistro tipoRegistro = TipoRegistro.QR_AUTO;

    @Column(columnDefinition = "DECIMAL(10,8)")
    private Double latitud;

    @Column(columnDefinition = "DECIMAL(11,8)")
    private Double longitud;

    /**
     * User-Agent del dispositivo que registró la asistencia.
     * Almacenado como TEXT para soportar User-Agents largos (hasta 65,535 caracteres).
     * Ejemplos típicos: navegadores móviles (200-300 chars), desktop (100-150 chars).
     */
    @Column(columnDefinition = "TEXT")
    private String dispositivo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(columnDefinition = "TEXT")
    private String notas;

    @PrePersist
    protected void onCreate() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }
}
