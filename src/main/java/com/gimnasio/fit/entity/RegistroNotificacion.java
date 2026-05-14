package com.gimnasio.fit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad para registrar notificaciones enviadas (HU-31).
 * Evita envíos duplicados/spam al guardar quién recibió qué notificación y cuándo.
 */
@Entity
@Table(name = "registro_notificaciones", indexes = {
    @Index(name = "idx_reg_notif_cliente", columnList = "cliente_id"),
    @Index(name = "idx_reg_notif_tipo_dias", columnList = "tipo, dias_antes"),
    @Index(name = "idx_reg_notif_fecha", columnList = "fecha_envio")
}, uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_notificacion_unica",
        columnNames = {"cliente_id", "tipo", "dias_antes", "fecha_vencimiento_referencia"}
    )
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroNotificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /**
     * Tipo de notificación: "EMAIL" o "SMS"
     */
    @Column(nullable = false, length = 10)
    private String tipo;

    /**
     * Días antes del vencimiento en que se envió (30, 15, 7, 1)
     */
    @Column(name = "dias_antes", nullable = false)
    private Integer diasAntes;

    /**
     * Fecha de vencimiento de referencia para evitar duplicados.
     * Si el cliente renueva y tiene nueva fecha, se permite re-enviar.
     */
    @Column(name = "fecha_vencimiento_referencia", nullable = false)
    private java.time.LocalDate fechaVencimientoReferencia;

    /**
     * Fecha y hora en que se envió la notificación.
     */
    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    /**
     * Indica si el envío fue exitoso.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean exitoso = true;

    /**
     * Mensaje de error si el envío falló.
     */
    @Column(name = "mensaje_error", length = 500)
    private String mensajeError;

    @PrePersist
    protected void onCreate() {
        if (fechaEnvio == null) {
            fechaEnvio = LocalDateTime.now();
        }
    }
}
