package com.gimnasio.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetricaComparativaDTO {
    private String nombre;           // "Total Asistencias", "Nuevos Clientes", etc.
    private String valorActual;      // "8,750", "145", "$45,680", "92%"
    private String valorAnterior;    // Valor del periodo anterior
    private Double porcentajeCambio; // +12.6, -3.1, etc.
    private String tendencia;        // "up", "down", "neutral"
    private String icono;            // "users", "activity", "dollar-sign"
    private String categoria;        // "asistencia", "suscripcion", "ingreso"
}
