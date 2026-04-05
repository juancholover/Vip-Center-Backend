package com.gimnasio.fit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Entidad que registra el historial de renovaciones de membresías.
 * Se inserta automáticamente cuando un cliente renueva su membresía.
 */
@Entity
@Table(name = "renovaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Renovacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "membresia_id", nullable = false)
    private Membresia membresia;

    @Column(name = "fecha_renovacion", nullable = false)
    private Instant fechaRenovacion = Instant.now();

    @Column(name = "dias_agregados", nullable = false)
    private Integer diasAgregados;

    @Column(name = "fecha_vencimiento_anterior")
    private LocalDate fechaVencimientoAnterior;

    @Column(name = "fecha_vencimiento_nueva", nullable = false)
    private LocalDate fechaVencimientoNueva;

    @Column(name = "monto_pagado", precision = 10, scale = 2)
    private BigDecimal montoPagado;

    @Column(name = "metodo_pago", length = 50)
    private String metodoPago;

    @Column(name = "observaciones", length = 255)
    private String observaciones;
}
