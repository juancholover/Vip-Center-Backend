package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private Integer clientesActivos;
    private Double ingresosMes;
    private Integer asistenciasHoy;
    private Integer membresiasPorVencer;
    private Integer promedioDiario; // Promedio de asistencias diarias del mes
}
