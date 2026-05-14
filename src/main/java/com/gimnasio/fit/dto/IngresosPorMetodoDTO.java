package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para ingresos agrupados por método de pago (HU-29).
 * Formato exacto requerido por el frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngresosPorMetodoDTO {
    private String metodo;       // "Tarjeta", "Yape/Plin", "Efectivo"
    private Double total;        // Monto total
    private Integer cantidad;    // Cantidad de transacciones
    private Double porcentaje;   // Porcentaje del total
}
