package com.gimnasio.fit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Catálogo de planes de membresía disponibles en el gimnasio.
 * Ejemplos: Mensual, Trimestral, Semestral, Anual.
 */
@Entity
@Table(name = "membresias", indexes = {
    @Index(name = "idx_membresias_estado", columnList = "estado"),
    @Index(name = "idx_membresias_orden", columnList = "orden")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Membresia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre descriptivo del plan (ej: "Membresía Mensual").
     */
    @Column(length = 100, nullable = false)
    private String nombre;

    /**
     * Descripción detallada de los beneficios del plan.
     */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Duración del plan en días (30, 90, 180, 365).
     */
    @Column(nullable = false)
    private Integer duracionDias;

    /**
     * Precio del plan en la moneda local.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    /**
     * Indica si la membresía está activa (disponible para venta).
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean estado = true;

    /**
     * Color para identificar el plan en el frontend (ej: #FF5733).
     */
    @Column(length = 20)
    private String color;

    /**
     * Orden de visualización (automático por duración: menor a mayor).
     */
    @Column
    @Builder.Default
    private Integer orden = 0;

    /**
     * Usuario que creó esta membresía.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    private Usuario creadoPor;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    @Builder.Default
    private Instant fechaCreacion = Instant.now();

    @Column(name = "fecha_modificacion")
    private Instant fechaModificacion;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = Instant.now();
    }
}
