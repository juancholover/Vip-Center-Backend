package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteMembresiaDTO {
    private Long membresiaId;
    private String nombreMembresia;
    private Double precioBase;
    private Integer duracionDias;
    private Integer cantidadVentas;
    private Double totalIngresos;
    private Double promedioIngresoMensual;
    private Integer clientesActivos;
    private Integer clientesVencidos;
    private Double tasaRetencion; 
}
