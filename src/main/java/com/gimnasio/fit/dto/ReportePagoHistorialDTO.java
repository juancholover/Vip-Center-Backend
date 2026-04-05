package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportePagoHistorialDTO {
    private Long pagoId;
    private String fecha;
    private String hora;
    private String cliente;
    private String plan;
    private String metodo;
    private Double monto;
    private String estado;
}
