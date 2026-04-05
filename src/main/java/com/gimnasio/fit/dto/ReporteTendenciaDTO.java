package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteTendenciaDTO {
    private String fecha;      // "1 Sep", "2024-09-01", etc
    private Integer valor;     // asistencias, ventas, etc
    private Double monto;      // para ingresos
}
