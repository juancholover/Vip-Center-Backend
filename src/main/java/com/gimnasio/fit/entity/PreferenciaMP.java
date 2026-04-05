package com.gimnasio.fit.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Representa una preferencia de pago creada en MercadoPago.
 * Se persiste al crear la preferencia para poder recuperar
 * initPoint, generar QR y enviar por WhatsApp.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "preferencias_mp", indexes = {
        @Index(name = "idx_pref_mp_preference_id", columnList = "preference_id", unique = true),
        @Index(name = "idx_pref_mp_cliente", columnList = "cliente_id"),
        @Index(name = "idx_pref_mp_estado", columnList = "estado")
})
public class PreferenciaMP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID de la preferencia retornado por MercadoPago.
     */
    @Column(name = "preference_id", nullable = false, length = 100, unique = true)
    private String preferenceId;

    /**
     * URL de pago (initPoint) retornado por MercadoPago.
     */
    @Column(name = "init_point", nullable = false, length = 500)
    private String initPoint;

    /**
     * Cliente asociado a esta preferencia.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    /**
     * Monto de la preferencia.
     */
    @Column(nullable = false, scale = 2, precision = 12)
    private BigDecimal monto;

    /**
     * Nombre del plan asociado (ej. "MENSUAL", "TRIMESTRAL").
     */
    @Column(name = "plan_nombre", length = 100)
    private String planNombre;

    /**
     * Días de duración del plan.
     */
    @Column(name = "plan_dias")
    private Integer planDias;
    
    /**
     * ID de la membresía asociada.
     */
    @Column(name = "membresia_id")
    private Long membresiaId;

    /**
     * Email del cliente (puede ser diferente al registrado).
     */
    @Column(name = "email_cliente", length = 100)
    private String emailCliente;

    /**
     * Estado de la preferencia: CREADA, PAGADA, EXPIRADA, CANCELADA.
     */
    @Column(nullable = false, length = 20)
    private String estado = "CREADA";

    /**
     * Fecha de creación de la preferencia.
     */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion = Instant.now();

    /**
     * Fecha de expiración calculada (opcional, puede calcularse desde MercadoPago).
     */
    @Column(name = "fecha_expiracion")
    private Instant fechaExpiracion;

    /**
     * Notas adicionales.
     */
    @Column(columnDefinition = "TEXT")
    private String notas;
}
