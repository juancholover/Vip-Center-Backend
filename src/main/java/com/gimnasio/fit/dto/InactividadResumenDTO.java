package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de respuesta agrupada para el panel de alertas de inactividad (HU-34).
 * Agrupa clientes por nivel de riesgo con conteo por categoría.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InactividadResumenDTO {
    private Integer totalInactivos;
    private Integer bajo;        // <15 días
    private Integer medio;       // 15-30 días
    private Integer alto;        // 30-60 días
    private Integer critico;     // >60 días
    private List<ClienteInactividadDTO> clientes;
}
