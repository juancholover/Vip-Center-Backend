package com.gimnasio.fit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidad que representa descuentos aplicables a membresías
 * Ordenados automáticamente por porcentaje (menor a mayor)
 */
@Entity
@Table(name = "descuentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Descuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentaje;

    @Column(nullable = false)
    private Integer orden = 0;

    @Column(nullable = false, columnDefinition = "BIT(1)")
    private Boolean estado = true;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "creado_por")
    private Long creadoPor;

    @Column(name = "modificado_por")
    private Long modificadoPor;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}
