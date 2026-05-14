package com.gimnasio.fit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidad para registrar el historial de pagos fallidos/rechazados (HU-33).
 * Permite que el administrador tenga visibilidad de pagos problemáticos.
 */
@Entity
@Table(name = "historial_pagos_fallidos", indexes = {
    @Index(name = "idx_pago_fallido_cliente", columnList = "cliente_id"),
    @Index(name = "idx_pago_fallido_fecha", columnList = "fecha_registro")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialPagoFallido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id")
    private Pago pago;

    /**
     * Estado del pago: "rechazado", "pendiente", "cancelled"
     */
    @Column(name = "estado_pago", nullable = false, length = 30)
    private String estadoPago;

    /**
     * Detalle del motivo del fallo (del proveedor de pago).
     */
    @Column(name = "motivo_detalle", length = 500)
    private String motivoDetalle;

    /**
     * Monto que se intentó pagar.
     */
    @Column(name = "monto_intentado", scale = 2, precision = 12)
    private BigDecimal montoIntentado;

    /**
     * Método de pago utilizado.
     */
    @Column(name = "metodo_pago", length = 30)
    private String metodoPago;

    /**
     * Si se envió notificación al cliente.
     */
    @Column(name = "notificacion_enviada", nullable = false)
    @Builder.Default
    private Boolean notificacionEnviada = false;

    /**
     * Fecha y hora del registro.
     */
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    @Builder.Default
    private Instant fechaRegistro = Instant.now();
}
