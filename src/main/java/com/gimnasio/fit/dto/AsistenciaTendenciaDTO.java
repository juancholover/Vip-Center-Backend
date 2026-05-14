package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para tendencia de asistencias (HU-26).
 * Representa la cantidad de asistencias en una fecha específica.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsistenciaTendenciaDTO {
    private String fecha;      // "Lun", "Mar", "01/05", etc.
    private Integer cantidad;  // Total de asistencias ese día
}
