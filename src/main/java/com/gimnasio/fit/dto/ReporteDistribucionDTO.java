package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDistribucionDTO {
    private String name;      // "Activas", "Vencidas", "Mensual"
    private Integer value;    // cantidad
    private String color;     // color para el gráfico
}
