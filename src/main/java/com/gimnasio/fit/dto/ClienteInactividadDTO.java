package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO para clientes inactivos con nivel de riesgo (HU-34).
 * Incluye información de contacto para campañas de marketing (HU-35).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteInactividadDTO {
    private Long clienteId;
    private String nombreCompleto;
    private String telefono;
    private String email;
    private String plan;                    // Nombre de la membresía
    private LocalDate fechaVencimiento;
    private LocalDateTime ultimaAsistencia; // Fecha de última asistencia (null = nunca)
    private Integer diasInactivo;           // Días desde la última asistencia
    private String nivelRiesgo;             // "BAJO" (<15d), "MEDIO" (15-30d), "ALTO" (30-60d), "CRITICO" (>60d)
    private String colorBadge;              // "green", "yellow", "orange", "red"
}
