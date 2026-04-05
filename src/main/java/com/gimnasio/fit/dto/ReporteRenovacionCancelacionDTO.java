package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteRenovacionCancelacionDTO {
    private String mes;              // "Ene", "Feb", "Mar", etc.
    private Integer renovaciones;     // Cantidad de renovaciones ese mes
    private Integer cancelaciones;    // Cantidad de cancelaciones ese mes
    private Integer anio;            // 2025
    private Integer mesNumero;       // 1-12
}
