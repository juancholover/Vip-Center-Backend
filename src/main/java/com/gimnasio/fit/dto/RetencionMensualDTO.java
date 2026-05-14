package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para reporte de retención mensual (HU-30).
 * Muestra renovaciones y cancelaciones por mes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RetencionMensualDTO {
    private Integer anio;           // 2026
    private Integer mesNumero;      // 1-12
    private String mes;             // "Enero", "Febrero", etc.
    private Integer renovaciones;   // Cantidad de renovaciones ese mes
    private Integer cancelaciones;  // Cantidad de cancelaciones ese mes
}
