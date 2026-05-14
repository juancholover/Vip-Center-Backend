package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para el reporte de suscripciones con filtros (HU-28).
 * Representa una suscripción/cliente con su estado actual.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteSuscripcionDTO {
    private Long clienteId;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String plan;              // Nombre de la membresía
    private LocalDate fechaVencimiento;
    private String estado;            // "ACTIVA", "VENCIDA", "POR_VENCER", "SIN_MEMBRESIA"
}
