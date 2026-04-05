package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteIngresosDTO {
    private String periodo;           // "Enero 2025", "2025-Q1", etc.
    private Double totalIngresos;
    private Integer cantidadPagos;
    private Double promedioTicket;
    private Double ingresosAprobados;
    private Double ingresosPendientes;
    private Double ingresosRechazados;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
}
