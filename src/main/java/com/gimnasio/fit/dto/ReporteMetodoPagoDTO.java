package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteMetodoPagoDTO {
    private String metodo;          // "Efectivo", "Tarjeta", "Transferencia"
    private Double total;           // 8240.00
    private Integer cantidad;       // Número de transacciones
    private Double porcentaje;      // 18.0 (del total)
}
