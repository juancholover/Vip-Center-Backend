package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para ingresos agrupados por plan de membresía (HU-29).
 * Formato exacto requerido por el frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngresosPorPlanDTO {
    private String plan;         // "Plan Anual", "Plan Mensual"
    private Double total;        // Monto total
    private Integer cantidad;    // Cantidad de transacciones
    private Double porcentaje;   // Porcentaje del total
}
